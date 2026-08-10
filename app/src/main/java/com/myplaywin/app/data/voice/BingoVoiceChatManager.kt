package com.myplaywin.app.data.voice

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import java.util.concurrent.ConcurrentHashMap

enum class VoiceConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class VoiceParticipant(
    val uid: String = "",
    val displayName: String = "",
    val isMuted: Boolean = false,
    val isSpeaking: Boolean = false,
    val joinedAt: Long = 0L
)

object BingoVoiceChatManager {
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val _connectionState = MutableStateFlow(VoiceConnectionState.DISCONNECTED)
    val connectionState: StateFlow<VoiceConnectionState> = _connectionState.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _speakingPlayers = MutableStateFlow<Set<String>>(emptySet())
    val speakingPlayers: StateFlow<Set<String>> = _speakingPlayers.asStateFlow()

    private val _roomParticipants = MutableStateFlow<Map<String, VoiceParticipant>>(emptyMap())
    val roomParticipants: StateFlow<Map<String, VoiceParticipant>> = _roomParticipants.asStateFlow()

    private var activeRoomCode: String? = null
    private var myUserId: String = ""
    private var myDisplayName: String = "Player"

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var audioDeviceModule: org.webrtc.audio.JavaAudioDeviceModule? = null
    private var audioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    private val peerConnections = ConcurrentHashMap<String, PeerConnection>()
    private val pendingIceCandidates = ConcurrentHashMap<String, MutableList<IceCandidate>>()

    private var dbRef: DatabaseReference? = null
    private var participantsListener: ValueEventListener? = null
    private var offersListener: ValueEventListener? = null
    private var answersListener: ValueEventListener? = null
    private var iceCandidatesListener: ValueEventListener? = null

    private var statsJob: Job? = null

    fun initializeWebRTC(context: Context) {
        if (peerConnectionFactory != null) return
        try {
            val initOptions = PeerConnectionFactory.InitializationOptions.builder(context.applicationContext)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(initOptions)

            val adm = org.webrtc.audio.JavaAudioDeviceModule.builder(context.applicationContext)
                .setUseHardwareAcousticEchoCanceler(true)
                .setUseHardwareNoiseSuppressor(true)
                .createAudioDeviceModule()
            audioDeviceModule = adm

            val options = PeerConnectionFactory.Options()
            peerConnectionFactory = PeerConnectionFactory.builder()
                .setAudioDeviceModule(adm)
                .setOptions(options)
                .createPeerConnectionFactory()

            val audioConstraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
            }

            audioSource = peerConnectionFactory?.createAudioSource(audioConstraints)
            localAudioTrack = peerConnectionFactory?.createAudioTrack("ARDAMSa0", audioSource)
            localAudioTrack?.setEnabled(!_isMuted.value)
        } catch (e: Exception) {
            android.util.Log.e("BingoVoiceChat", "Failed to initialize WebRTC engine", e)
        }
    }

    fun joinVoiceRoom(context: Context, roomCode: String, displayName: String = "Player"): Boolean {
        val cleanRoomCode = roomCode.trim().uppercase()
        if (cleanRoomCode.isBlank()) return false

        // Check record audio permission
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            _connectionState.value = VoiceConnectionState.ERROR
            _statusMessage.value = "Microphone permission required"
            return false
        }

        if (activeRoomCode == cleanRoomCode && _connectionState.value == VoiceConnectionState.CONNECTED) {
            return true
        }

        // Leave previous room if any
        if (activeRoomCode != null && activeRoomCode != cleanRoomCode) {
            leaveVoiceRoom(context)
        }

        _connectionState.value = VoiceConnectionState.CONNECTING
        _statusMessage.value = "Connecting..."
        activeRoomCode = cleanRoomCode

        val auth = FirebaseAuth.getInstance()
        myUserId = auth.currentUser?.uid ?: "user_${System.currentTimeMillis().toString().takeLast(6)}"
        myDisplayName = displayName

        try {
            initializeWebRTC(context)
            localAudioTrack?.setEnabled(!_isMuted.value)

            // Audio Manager config for communication
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager?.isSpeakerphoneOn = true

            val database = FirebaseDatabase.getInstance()
            dbRef = database.getReference("private_voice_rooms").child(cleanRoomCode)

            // Register presence
            val myParticipantRef = dbRef?.child("participants")?.child(myUserId)
            val pData = HashMap<String, Any>()
            pData["uid"] = myUserId
            pData["displayName"] = myDisplayName
            pData["isMuted"] = _isMuted.value
            pData["isSpeaking"] = false
            pData["joinedAt"] = ServerValue.TIMESTAMP

            myParticipantRef?.setValue(pData)
            myParticipantRef?.onDisconnect()?.removeValue()

            setupListeners(context, cleanRoomCode)
            startStatsAndSpeakingCheck()

            _connectionState.value = VoiceConnectionState.CONNECTED
            _statusMessage.value = "Voice Connected"
            return true
        } catch (e: Exception) {
            android.util.Log.e("BingoVoiceChat", "Error joining voice room $cleanRoomCode", e)
            _connectionState.value = VoiceConnectionState.ERROR
            _statusMessage.value = "Voice unavailable"
            return false
        }
    }

    private fun setupListeners(context: Context, roomCode: String) {
        val rootRef = dbRef ?: return

        // 1. Participant List Listener
        participantsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newParticipants = mutableMapOf<String, VoiceParticipant>()
                val newSpeaking = mutableSetOf<String>()

                for (child in snapshot.children) {
                    val uid = child.child("uid").getValue(String::class.java) ?: child.key ?: continue
                    val name = child.child("displayName").getValue(String::class.java) ?: "Player"
                    val muted = child.child("isMuted").getValue(Boolean::class.java) ?: false
                    val speaking = child.child("isSpeaking").getValue(Boolean::class.java) ?: false
                    val joinedAt = child.child("joinedAt").getValue(Long::class.java) ?: 0L

                    val vp = VoiceParticipant(uid, name, muted, speaking, joinedAt)
                    newParticipants[uid] = vp

                    if (speaking && !muted) {
                        newSpeaking.add(uid)
                    }

                    // Initiate connection if peer is other user
                    if (uid != myUserId && !peerConnections.containsKey(uid)) {
                        createPeerConnectionForUser(context, uid)
                        if (myUserId < uid) {
                            initiateCallOffer(uid)
                        }
                    }
                }

                _roomParticipants.value = newParticipants
                _speakingPlayers.value = newSpeaking

                // Clean up left peers
                val currentPeerUids = ArrayList(peerConnections.keys)
                for (peerUid in currentPeerUids) {
                    if (!newParticipants.containsKey(peerUid)) {
                        peerConnections[peerUid]?.close()
                        peerConnections.remove(peerUid)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("BingoVoiceChat", "Participants cancelled: ${error.message}")
            }
        }
        rootRef.child("participants").addValueEventListener(participantsListener!!)

        // 2. Incoming Offers Listener
        offersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (offerSnap in snapshot.children) {
                    val senderUid = offerSnap.key ?: continue
                    val sdpStr = offerSnap.child("sdp").getValue(String::class.java) ?: continue
                    val typeStr = offerSnap.child("type").getValue(String::class.java) ?: "OFFER"

                    if (senderUid != myUserId) {
                        handleIncomingOffer(context, senderUid, sdpStr, typeStr)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        rootRef.child("signaling").child("offers").child(myUserId).addValueEventListener(offersListener!!)

        // 3. Incoming Answers Listener
        answersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ansSnap in snapshot.children) {
                    val senderUid = ansSnap.key ?: continue
                    val sdpStr = ansSnap.child("sdp").getValue(String::class.java) ?: continue
                    val typeStr = ansSnap.child("type").getValue(String::class.java) ?: "ANSWER"

                    if (senderUid != myUserId) {
                        handleIncomingAnswer(senderUid, sdpStr, typeStr)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        rootRef.child("signaling").child("answers").child(myUserId).addValueEventListener(answersListener!!)

        // 4. Incoming ICE Candidates Listener
        iceCandidatesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (senderSnap in snapshot.children) {
                    val senderUid = senderSnap.key ?: continue
                    if (senderUid == myUserId) continue

                    for (candidateSnap in senderSnap.children) {
                        val sdpMid = candidateSnap.child("sdpMid").getValue(String::class.java) ?: ""
                        val sdpMLineIndex = candidateSnap.child("sdpMLineIndex").getValue(Int::class.java) ?: 0
                        val candidateSdp = candidateSnap.child("candidate").getValue(String::class.java) ?: ""

                        if (candidateSdp.isNotBlank()) {
                            val iceCandidate = IceCandidate(sdpMid, sdpMLineIndex, candidateSdp)
                            addIceCandidateToPeer(senderUid, iceCandidate)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        rootRef.child("signaling").child("ice_candidates").child(myUserId).addValueEventListener(iceCandidatesListener!!)
    }

    private fun addIceCandidateToPeer(peerUid: String, candidate: IceCandidate) {
        val pc = peerConnections[peerUid]
        if (pc != null && pc.remoteDescription != null) {
            pc.addIceCandidate(candidate)
        } else {
            val list = pendingIceCandidates.getOrPut(peerUid) { mutableListOf() }
            synchronized(list) {
                list.add(candidate)
            }
        }
    }

    private fun drainPendingIceCandidates(peerUid: String) {
        val pc = peerConnections[peerUid] ?: return
        val candidates = pendingIceCandidates.remove(peerUid) ?: return
        synchronized(candidates) {
            for (candidate in candidates) {
                pc.addIceCandidate(candidate)
            }
        }
    }

    private fun createPeerConnectionForUser(context: Context, peerUid: String): PeerConnection? {
        if (peerConnections.containsKey(peerUid)) {
            return peerConnections[peerUid]
        }

        val factory = peerConnectionFactory ?: return null

        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun3.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun4.l.google.com:19302").createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }

        val observer = object : SimplePeerConnectionObserver() {
            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate ?: return
                val candidateMap = HashMap<String, Any>()
                candidateMap["sdpMid"] = candidate.sdpMid
                candidateMap["sdpMLineIndex"] = candidate.sdpMLineIndex
                candidateMap["candidate"] = candidate.sdp

                dbRef?.child("signaling")
                    ?.child("ice_candidates")
                    ?.child(peerUid)
                    ?.child(myUserId)
                    ?.push()
                    ?.setValue(candidateMap)
            }

            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                android.util.Log.d("BingoVoiceChat", "IceConnectionState with $peerUid: $state")
            }
        }

        val pc = factory.createPeerConnection(rtcConfig, observer) ?: return null

        localAudioTrack?.let { track ->
            pc.addTrack(track, listOf("ARDAMS"))
        }

        peerConnections[peerUid] = pc
        return pc
    }

    private fun initiateCallOffer(peerUid: String) {
        val pc = peerConnections[peerUid] ?: return
        val mediaConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        }

        pc.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp ?: return
                pc.setLocalDescription(SimpleSdpObserver(), sdp)

                val offerMap = HashMap<String, Any>()
                offerMap["sdp"] = sdp.description
                offerMap["type"] = sdp.type.canonicalForm()

                dbRef?.child("signaling")
                    ?.child("offers")
                    ?.child(peerUid)
                    ?.child(myUserId)
                    ?.setValue(offerMap)
            }
        }, mediaConstraints)
    }

    private fun handleIncomingOffer(context: Context, senderUid: String, sdpStr: String, typeStr: String) {
        val pc = createPeerConnectionForUser(context, senderUid) ?: return
        val sdp = SessionDescription(SessionDescription.Type.fromCanonicalForm(typeStr.lowercase()), sdpStr)

        pc.setRemoteDescription(object : SimpleSdpObserver() {
            override fun onSetSuccess() {
                drainPendingIceCandidates(senderUid)
                val mediaConstraints = MediaConstraints().apply {
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
                }

                pc.createAnswer(object : SimpleSdpObserver() {
                    override fun onCreateSuccess(answerSdp: SessionDescription?) {
                        answerSdp ?: return
                        pc.setLocalDescription(SimpleSdpObserver(), answerSdp)

                        val answerMap = HashMap<String, Any>()
                        answerMap["sdp"] = answerSdp.description
                        answerMap["type"] = answerSdp.type.canonicalForm()

                        dbRef?.child("signaling")
                            ?.child("answers")
                            ?.child(senderUid)
                            ?.child(myUserId)
                            ?.setValue(answerMap)
                    }
                }, mediaConstraints)
            }
        }, sdp)
    }

    private fun handleIncomingAnswer(senderUid: String, sdpStr: String, typeStr: String) {
        val pc = peerConnections[senderUid] ?: return
        val sdp = SessionDescription(SessionDescription.Type.fromCanonicalForm(typeStr.lowercase()), sdpStr)
        pc.setRemoteDescription(object : SimpleSdpObserver() {
            override fun onSetSuccess() {
                drainPendingIceCandidates(senderUid)
            }
        }, sdp)
    }

    fun toggleMute() {
        val newMute = !_isMuted.value
        _isMuted.value = newMute
        localAudioTrack?.setEnabled(!newMute)

        if (myUserId.isNotBlank() && dbRef != null) {
            dbRef?.child("participants")?.child(myUserId)?.child("isMuted")?.setValue(newMute)
            if (newMute) {
                dbRef?.child("participants")?.child(myUserId)?.child("isSpeaking")?.setValue(false)
            }
        }
    }

    private fun startStatsAndSpeakingCheck() {
        statsJob?.cancel()
        statsJob = scope.launch {
            while (_connectionState.value == VoiceConnectionState.CONNECTED) {
                try {
                    if (!_isMuted.value && myUserId.isNotBlank() && dbRef != null) {
                        var maxAudioLevel = 0.0
                        val activePeerUids = ArrayList(peerConnections.keys)
                        for (idx in 0 until activePeerUids.size) {
                            val pUid = activePeerUids[idx]
                            val pc = peerConnections[pUid] ?: continue
                            pc.getStats(object : org.webrtc.RTCStatsCollectorCallback {
                                override fun onStatsDelivered(report: org.webrtc.RTCStatsReport?) {
                                    if (report != null) {
                                        val statsMap = report.statsMap
                                        if (statsMap != null) {
                                            for ((_, stats) in statsMap) {
                                                val members = stats.members
                                                if (members != null) {
                                                    val level = members["audioInputLevel"] ?: members["audioOutputLevel"]
                                                    (level as? Number)?.toDouble()?.let { lvl ->
                                                        if (lvl > maxAudioLevel) maxAudioLevel = lvl
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            })
                        }

                        val isSpeakingNow = maxAudioLevel > 200.0
                        dbRef?.child("participants")?.child(myUserId)?.child("isSpeaking")?.setValue(isSpeakingNow)
                    }
                } catch (e: Exception) {
                    // Ignore stats errors
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    fun leaveVoiceRoom(context: Context) {
        try {
            statsJob?.cancel()
            statsJob = null

            // Remove listeners
            participantsListener?.let { dbRef?.child("participants")?.removeEventListener(it) }
            offersListener?.let { dbRef?.child("signaling")?.child("offers")?.child(myUserId)?.removeEventListener(it) }
            answersListener?.let { dbRef?.child("signaling")?.child("answers")?.child(myUserId)?.removeEventListener(it) }
            iceCandidatesListener?.let { dbRef?.child("signaling")?.child("ice_candidates")?.child(myUserId)?.removeEventListener(it) }

            participantsListener = null
            offersListener = null
            answersListener = null
            iceCandidatesListener = null

            // Remove presence
            if (myUserId.isNotBlank() && dbRef != null) {
                dbRef?.child("participants")?.child(myUserId)?.removeValue()
            }

            // Close peer connections
            for ((_, pc) in peerConnections) {
                pc.close()
            }
            peerConnections.clear()

            // Disable local microphone capture
            localAudioTrack?.setEnabled(false)

            // Reset Audio Manager mode
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.mode = AudioManager.MODE_NORMAL
            audioManager?.isSpeakerphoneOn = false

            activeRoomCode = null
            dbRef = null

            _connectionState.value = VoiceConnectionState.DISCONNECTED
            _statusMessage.value = ""
            _speakingPlayers.value = emptySet()
            _roomParticipants.value = emptyMap()
        } catch (e: Exception) {
            android.util.Log.e("BingoVoiceChat", "Error leaving voice room", e)
        }
    }
}

private open class SimplePeerConnectionObserver : PeerConnection.Observer {
    override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
    override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {}
    override fun onIceConnectionReceivingChange(receiving: Boolean) {}
    override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
    override fun onIceCandidate(candidate: IceCandidate?) {}
    override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
    override fun onAddStream(stream: MediaStream?) {
        stream?.audioTracks?.forEach { track ->
            track.setEnabled(true)
            track.setVolume(1.0)
        }
    }
    override fun onRemoveStream(stream: MediaStream?) {}
    override fun onDataChannel(channel: DataChannel?) {}
    override fun onRenegotiationNeeded() {}
    override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {
        receiver?.track()?.let { track ->
            track.setEnabled(true)
            if (track is AudioTrack) {
                track.setVolume(1.0)
            }
        }
        streams?.forEach { stream ->
            stream.audioTracks?.forEach { track ->
                track.setEnabled(true)
                track.setVolume(1.0)
            }
        }
    }
}

private open class SimpleSdpObserver : SdpObserver {
    override fun onCreateSuccess(sdp: SessionDescription?) {}
    override fun onSetSuccess() {}
    override fun onCreateFailure(error: String?) {}
    override fun onSetFailure(error: String?) {}
}
