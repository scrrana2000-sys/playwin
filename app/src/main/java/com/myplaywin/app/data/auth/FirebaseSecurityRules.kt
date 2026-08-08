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
    }
  }
}
""".trimIndent()
}
