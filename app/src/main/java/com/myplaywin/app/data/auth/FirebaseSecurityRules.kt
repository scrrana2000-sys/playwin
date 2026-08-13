package com.myplaywin.app.data.auth

/**
 * Phase 7: Firebase Security Rules & Anti-Cheat Validation Rules Infrastructure
 */
object FirebaseSecurityRules {

    val FIRESTORE_SECURITY_RULES: String = """
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // User Profiles
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }

    // Real-Time Bingo Rooms
    match /bingo_online_rooms/{roomId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && request.resource.data.matchStatus in ['SEARCHING', 'MATCHED'];
      allow update: if request.auth != null && (
        resource.data.player1.uid == request.auth.uid || 
        resource.data.player2.uid == request.auth.uid
      );
      allow delete: if false;
    }

    // Matchmaking Queue
    match /online_matchmaking_queue/{queueId} {
      allow read, write: if request.auth != null && request.auth.uid == queueId;
    }
  }
}
""".trimIndent()

    val REALTIME_DATABASE_RULES: String = """
{
  "rules": {
    "bingo_rooms": {
      "${'$'}roomId": {
        ".read": "auth != null",
        ".write": "auth != null && (data.child('player1/uid').val() === auth.uid || data.child('player2/uid').val() === auth.uid)",
        "calledNumbersHistory": {
          ".validate": "newData.hasChildren()"
        }
      }
    },
    "presence": {
      "${'$'}uid": {
        ".read": "auth != null",
        ".write": "auth != null && auth.uid === ${'$'}uid"
      }
    },
    "users": {
      "${'$'}uid": {
        ".read": "auth != null",
        ".write": "auth != null && auth.uid === ${'$'}uid"
      }
    },
    "adTelemetry": {
      "${'$'}uid": {
        ".read": "auth != null && (auth.uid === ${'$'}uid || root.child('users').child(auth.uid).child('role').val() === 'admin')",
        "${'$'}eventId": {
          ".write": "auth != null && auth.uid === ${'$'}uid && newData.exists() && (!data.exists() || (newData.child('uid').val() === data.child('uid').val() && newData.child('eventId').val() === data.child('eventId').val() && newData.child('timestamp').val() === data.child('timestamp').val() && newData.child('adUnitId').val() === data.child('adUnitId').val() && newData.child('source').val() === data.child('source').val() && (!data.child('valueMicros').exists() || data.child('valueMicros').val() === 0 || newData.child('valueMicros').val() === data.child('valueMicros').val())))",
          ".validate": "newData.child('uid').val() === auth.uid"
        }
      }
    },
    "coinRewardEvents": {
      "${'$'}uid": {
        ".read": "auth != null && (auth.uid === ${'$'}uid || root.child('users').child(auth.uid).child('role').val() === 'admin')",
        "${'$'}eventId": {
          ".write": "auth != null && auth.uid === ${'$'}uid && !data.exists() && newData.exists()",
          ".validate": "newData.child('uid').val() === auth.uid"
        }
      }
    },
    "gameTelemetry": {
      "${'$'}uid": {
        ".read": "auth != null && (auth.uid === ${'$'}uid || root.child('users').child(auth.uid).child('role').val() === 'admin')",
        "${'$'}eventId": {
          ".write": "auth != null && auth.uid === ${'$'}uid && !data.exists() && newData.exists()",
          ".validate": "newData.child('uid').val() === auth.uid"
        }
      }
    },
    "userSessions": {
      "${'$'}uid": {
        ".read": "auth != null && (auth.uid === ${'$'}uid || root.child('users').child(auth.uid).child('role').val() === 'admin')",
        "${'$'}sessionId": {
          ".write": "auth != null && auth.uid === ${'$'}uid && newData.exists()",
          ".validate": "newData.child('uid').val() === auth.uid"
        }
      }
    },
    "userDailyEconomy": {
      "${'$'}uid": {
        ".read": "auth != null && (auth.uid === ${'$'}uid || root.child('users').child(auth.uid).child('role').val() === 'admin')",
        "${'$'}date": {
          ".write": "auth != null && auth.uid === ${'$'}uid && newData.exists()",
          ".validate": "newData.child('uid').val() === auth.uid || !newData.child('uid').exists()",
          "coinsEarned": {
            ".validate": "newData.val() <= 100000"
          },
          "reportedRevenueMicros": {
            ".validate": "newData.val() <= 5000000"
          },
          "totalAdImpressions": {
            ".validate": "newData.val() <= 1000"
          }
        }
      }
    }
  }
}
""".trimIndent()
}
