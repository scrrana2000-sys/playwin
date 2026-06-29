package com.playwin.app.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.playwin.app.data.model.FirebaseCoupon
import com.playwin.app.data.model.FirebaseRedemption
import com.playwin.app.data.model.FirebaseReferral
import com.playwin.app.data.model.FirebaseTask
import com.playwin.app.data.model.FirebaseTransaction
import com.playwin.app.data.model.FirebaseUser
import com.playwin.app.data.model.Quiz
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume

class FirebaseDbManager {
    private val database = FirebaseDatabase.getInstance("https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val tasksRef = database.getReference("tasks")
    private val couponsRef = database.getReference("coupons")
    private val usersRef = database.getReference("users")
    private val transactionsRef = database.getReference("transactions")
    private val referralsRef = database.getReference("referrals")

    init {
        seedInitialDataIfNeeded()
    }

    private fun seedInitialDataIfNeeded() {
        tasksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                    val initialTasks = listOf(
                        FirebaseTask("task_profile", "Complete Profile", "Complete your profile to earn coins", "+5 Coins", 5, "profile"),
                        FirebaseTask("task_share", "Share App", "Share Play Win with your friends", "+10 Coins", 10, "share"),
                        FirebaseTask("task_video", "Watch 1 Video Ad", "Watch video ad and earn coins", "+50 Coins", 50, "video"),
                        FirebaseTask("task_invite", "Refer & Earn", "Invite friends & earn rewards", "+100 Coins", 100, "referral"),
                        FirebaseTask("task_daily", "Claim Today's Daily Check-In", "Available once per day", "+5 Coins", 5, "daily"),
                        FirebaseTask("task_streak", "7-Day Streak Complete", "Claim after 7 consecutive check-ins", "+50 Coins", 50, "streak"),
                        FirebaseTask("task_spin", "Play Lucky Spin & Win", "Test your luck", "Varies", 0, "spin"),
                        FirebaseTask("task_scratch", "Scratch Card Treasure", "Scratch & reveal rewards", "Varies", 0, "scratch"),
                        FirebaseTask("task_trivia", "Answer Trivia Challenge", "Answer questions accurately", "Varies", 0, "trivia"),
                        FirebaseTask("task_math", "Solve Speed Math Quiz", "Test your mental arithmetic", "Varies", 0, "math")
                    )
                    for (task in initialTasks) {
                        tasksRef.child(task.id).setValue(task)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        couponsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                    val initialCoupons = listOf(
                        FirebaseCoupon(
                            id = "coupon_amazon",
                            title = "Amazon Gift Card",
                            cost = 100,
                            code = "AMZ-PW-100",
                            description = "Redeem for Amazon Pay Gift Card. Use for shopping, bills, and payments.",
                            category = "Gift Cards",
                            availability = "In Stock",
                            expiryDate = "31 Dec 2026",
                            image = "🛍️",
                            stock = 50
                        ),
                        FirebaseCoupon(
                            id = "coupon_flipkart",
                            title = "Flipkart Gift Card",
                            cost = 200,
                            code = "FK-PW-200",
                            description = "Redeem for Flipkart Gift Card. Shop from millions of items with free delivery options.",
                            category = "Gift Cards",
                            availability = "In Stock",
                            expiryDate = "31 Dec 2026",
                            image = "🛒",
                            stock = 40
                        ),
                        FirebaseCoupon(
                            id = "coupon_google",
                            title = "Google Play Gift Card",
                            cost = 150,
                            code = "GP-PW-150",
                            description = "Get credits in Google Play Store. Buy premium apps, books, movies, or in-app items.",
                            category = "Gift Cards",
                            availability = "In Stock",
                            expiryDate = "31 Dec 2026",
                            image = "🎮",
                            stock = 30
                        ),
                        FirebaseCoupon(
                            id = "coupon_jiohotstar",
                            title = "JioHotstar",
                            cost = 500,
                            code = "JH-PW-500",
                            description = "Enjoy premium movies, sports, and TV shows with JioHotstar subscription.",
                            category = "Subscriptions",
                            availability = "In Stock",
                            expiryDate = "31 Dec 2026",
                            image = "🎬",
                            stock = 15
                        ),
                        FirebaseCoupon(
                            id = "coupon_netflix",
                            title = "Netflix",
                            cost = 800,
                            code = "NF-PW-800",
                            description = "Unlock Netflix Premium subscription for top-tier films and web series.",
                            category = "Subscriptions",
                            availability = "In Stock",
                            expiryDate = "31 Dec 2026",
                            image = "🍿",
                            stock = 10
                        ),
                        FirebaseCoupon(
                            id = "coupon_prime",
                            title = "Amazon Prime",
                            cost = 600,
                            code = "AP-PW-600",
                            description = "Get free fast delivery, Prime Video, Prime Music, and exclusive deals.",
                            category = "Subscriptions",
                            availability = "In Stock",
                            expiryDate = "31 Dec 2026",
                            image = "👑",
                            stock = 20
                        ),
                        FirebaseCoupon(
                            id = "coupon_sonyliv",
                            title = "Sony LIV",
                            cost = 400,
                            code = "SL-PW-400",
                            description = "Watch exclusive sports events, original shows, and movies on Sony LIV.",
                            category = "Subscriptions",
                            availability = "In Stock",
                            expiryDate = "31 Dec 2026",
                            image = "📺",
                            stock = 25
                        ),
                        FirebaseCoupon(
                            id = "coupon_zee5",
                            title = "ZEE5",
                            cost = 300,
                            code = "Z5-PW-300",
                            description = "Get access to unlimited blockbuster movies and exclusive web originals.",
                            category = "Subscriptions",
                            availability = "In Stock",
                            expiryDate = "31 Dec 2026",
                            image = "📱",
                            stock = 35
                        )
                    )
                    for (coupon in initialCoupons) {
                        couponsRef.child(coupon.id).setValue(coupon)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        val questionsRef = database.getReference("questions")
        questionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                    val pool = mapOf(
                        "GK" to listOf(
                            Quiz("q_gk_1", "Which country is home to the Kangaroo?", listOf("South Africa", "Australia", "New Zealand", "India"), 1),
                            Quiz("q_gk_2", "Which planet is known as the Red Planet?", listOf("Earth", "Mars", "Jupiter", "Venus"), 1),
                            Quiz("q_gk_3", "Which is the largest ocean on Earth?", listOf("Atlantic Ocean", "Indian Ocean", "Pacific Ocean", "Arctic Ocean"), 2),
                            Quiz("q_gk_4", "What is the capital of France?", listOf("London", "Rome", "Berlin", "Paris"), 3),
                            Quiz("q_gk_5", "What is the tallest mountain on Earth?", listOf("K2", "Mount Everest", "Kangchenjunga", "Lhotse"), 1),
                            Quiz("q_gk_6", "Who painted the Mona Lisa?", listOf("Michelangelo", "Leonardo da Vinci", "Vincent van Gogh", "Pablo Picasso"), 1),
                            Quiz("q_gk_7", "Which is the largest country by land area?", listOf("Canada", "Russia", "USA", "China"), 1),
                            Quiz("q_gk_8", "How many continents are there on Earth?", listOf("5", "6", "7", "8"), 2),
                            Quiz("q_gk_9", "Which of these is the currency of Japan?", listOf("Yuan", "Euro", "Won", "Yen"), 3),
                            Quiz("q_gk_10", "What is the hardest natural substance on Earth?", listOf("Gold", "Iron", "Diamond", "Platinum"), 2),
                            Quiz("q_gk_11", "Which river is the longest in the world?", listOf("Nile River", "Amazon River", "Yangtze River", "Mississippi River"), 0),
                            Quiz("q_gk_12", "Which country invented Tea?", listOf("India", "China", "Japan", "United Kingdom"), 1),
                            Quiz("q_gk_13", "Who is known as the Father of the Nation in India?", listOf("Nehru", "Subhas Chandra Bose", "Mahatma Gandhi", "Bhagat Singh"), 2),
                            Quiz("q_gk_14", "Which animal is known as the Ship of the Desert?", listOf("Horse", "Camel", "Elephant", "Donkey"), 1),
                            Quiz("q_gk_15", "How many primary colors are there?", listOf("2", "3", "4", "5"), 1)
                        ),
                        "Sports" to listOf(
                            Quiz("q_sports_1", "How many players are there in a standard soccer team?", listOf("9", "10", "11", "12"), 2),
                            Quiz("q_sports_2", "Which country won the FIFA World Cup in 2022?", listOf("France", "Argentina", "Brazil", "Croatia"), 1),
                            Quiz("q_sports_3", "Which sport is associated with Wimbledon?", listOf("Golf", "Tennis", "Cricket", "Polo"), 1),
                            Quiz("q_sports_4", "What is the national game of India?", listOf("Cricket", "Kabaddi", "Hockey", "Football"), 2),
                            Quiz("q_sports_5", "How many rings are there on the Olympic flag?", listOf("4", "5", "6", "7"), 1),
                            Quiz("q_sports_6", "Who is often called 'The Lightning Bolt'?", listOf("Usain Bolt", "Tyson Gay", "Yohan Blake", "Justin Gatlin"), 0),
                            Quiz("q_sports_7", "Which sport uses a shuttlecock?", listOf("Table Tennis", "Tennis", "Badminton", "Squash"), 2),
                            Quiz("q_sports_8", "How long is a marathon in kilometers?", listOf("21.1 km", "42.195 km", "50 km", "10 km"), 1),
                            Quiz("q_sports_9", "Who has won the most Ballon d'Or awards?", listOf("Cristiano Ronaldo", "Lionel Messi", "Neymar", "Pele"), 1),
                            Quiz("q_sports_10", "Which country is the birthplace of Cricket?", listOf("Australia", "India", "England", "South Africa"), 2),
                            Quiz("q_sports_11", "Which of these sports does NOT use a ball?", listOf("Basketball", "Golf", "Ice Hockey", "Baseball"), 2),
                            Quiz("q_sports_12", "How many years are there between consecutive Olympic Games?", listOf("2", "3", "4", "5"), 2),
                            Quiz("q_sports_13", "Who is known as the King of Clay in Tennis?", listOf("Roger Federer", "Rafael Nadal", "Novak Djokovic", "Andy Murray"), 1),
                            Quiz("q_sports_14", "What color is the center circle of an archery target?", listOf("Red", "Blue", "Yellow", "White"), 2),
                            Quiz("q_sports_15", "In which sport can you get a strike?", listOf("Golf", "Bowling", "Baseball", "Billiards"), 1)
                        ),
                        "Movies" to listOf(
                            Quiz("q_movies_1", "Who directed the movie 'Titanic'?", listOf("Steven Spielberg", "James Cameron", "Christopher Nolan", "Martin Scorsese"), 1),
                            Quiz("q_movies_2", "Which film won the Best Picture Oscar in 2020?", listOf("1917", "Parasite", "Joker", "Once Upon a Time in Hollywood"), 1),
                            Quiz("q_movies_3", "Who played Iron Man in the Marvel Cinematic Universe?", listOf("Chris Evans", "Robert Downey Jr.", "Chris Hemsworth", "Mark Ruffalo"), 1),
                            Quiz("q_movies_4", "What is the highest-grossing film of all time (unadjusted)?", listOf("Avatar", "Avengers: Endgame", "Titanic", "Star Wars: The Force Awakens"), 0),
                            Quiz("q_movies_5", "Which actor played Jack in 'Titanic'?", listOf("Brad Pitt", "Tom Cruise", "Leonardo DiCaprio", "Johnny Depp"), 2),
                            Quiz("q_movies_6", "In which movie did the character Joker say: 'Why so serious?'?", listOf("Batman Begins", "The Dark Knight", "The Dark Knight Rises", "Joker"), 1),
                            Quiz("q_movies_7", "Which actor played Captain Jack Sparrow?", listOf("Johnny Depp", "Orlando Bloom", "Keira Knightley", "Geoffrey Rush"), 0),
                            Quiz("q_movies_8", "How many Star Wars movies make up the Skywalker Saga?", listOf("6", "8", "9", "12"), 2),
                            Quiz("q_movies_9", "Who directed the sci-fi movie 'Inception'?", listOf("Christopher Nolan", "Quentin Tarantino", "David Fincher", "Ridley Scott"), 0),
                            Quiz("q_movies_10", "What was Disney's first full-length animated film?", listOf("Cinderella", "Snow White", "Pinocchio", "Bambi"), 1),
                            Quiz("q_movies_11", "Which movie features a friendly ghost named Casper?", listOf("Ghostbusters", "Casper", "Hocus Pocus", "Beetlejuice"), 1),
                            Quiz("q_movies_12", "Which country produces Bollywood movies?", listOf("USA", "UK", "India", "Nigeria"), 2),
                            Quiz("q_movies_13", "What is the name of the kingdom in 'Frozen'?", listOf("Corona", "Arendelle", "DunBroch", "Genovia"), 1),
                            Quiz("q_movies_14", "Who played Hermione Granger in the Harry Potter series?", listOf("Emma Watson", "Kristen Stewart", "Jennifer Lawrence", "Saoirse Ronan"), 0),
                            Quiz("q_movies_15", "Which actor starred as Neo in 'The Matrix'?", listOf("Keanu Reeves", "Laurence Fishburne", "Hugo Weaving", "Carrie-Anne Moss"), 0)
                        ),
                        "Science" to listOf(
                            Quiz("q_science_1", "What is the chemical symbol for Water?", listOf("H2O", "CO2", "NaCl", "O2"), 0),
                            Quiz("q_science_2", "Which gas is most abundant in Earth's atmosphere?", listOf("Oxygen", "Hydrogen", "Carbon Dioxide", "Nitrogen"), 3),
                            Quiz("q_science_3", "What is the speed of light?", listOf("150,000 km/s", "300,000 km/s", "500,000 km/s", "1,000,000 km/s"), 1),
                            Quiz("q_science_4", "Which organ is responsible for pumping blood?", listOf("Brain", "Lungs", "Heart", "Liver"), 2),
                            Quiz("q_science_5", "What is the center of an atom called?", listOf("Proton", "Neutron", "Nucleus", "Electron"), 2),
                            Quiz("q_science_6", "Which vitamin do we get from Sunlight?", listOf("Vitamin A", "Vitamin B", "Vitamin C", "Vitamin D"), 3),
                            Quiz("q_science_7", "Which gas do plants absorb during photosynthesis?", listOf("Oxygen", "Carbon Dioxide", "Nitrogen", "Argon"), 1),
                            Quiz("q_science_8", "How many bones are there in an adult human body?", listOf("204", "206", "208", "210"), 1),
                            Quiz("q_science_9", "Who proposed the Theory of Relativity?", listOf("Isaac Newton", "Albert Einstein", "Galileo Galilei", "Stephen Hawking"), 1),
                            Quiz("q_science_10", "What is the boiling point of water at sea level?", listOf("90°C", "100°C", "110°C", "120°C"), 1),
                            Quiz("q_science_11", "Which is the smallest unit of life?", listOf("Cell", "Tissue", "Atom", "Organ"), 0),
                            Quiz("q_science_12", "What is the closest star to Earth?", listOf("Sirius", "Alpha Centauri", "The Sun", "Proxima Centauri"), 2),
                            Quiz("q_science_13", "What force pulls objects toward the center of the Earth?", listOf("Friction", "Magnetism", "Gravity", "Inertia"), 2),
                            Quiz("q_science_14", "Which metal is liquid at room temperature?", listOf("Iron", "Copper", "Mercury", "Gold"), 2),
                            Quiz("q_science_15", "How many colors are there in a rainbow?", listOf("5", "6", "7", "8"), 2)
                        ),
                        "History" to listOf(
                            Quiz("q_history_1", "Who was the first President of the United States?", listOf("Thomas Jefferson", "George Washington", "Abraham Lincoln", "John Adams"), 1),
                            Quiz("q_history_2", "In which year did World War II end?", listOf("1918", "1939", "1945", "1950"), 2),
                            Quiz("q_history_3", "Which ancient civilization built the Pyramids?", listOf("Romans", "Greeks", "Egyptians", "Mayans"), 2),
                            Quiz("q_history_4", "Who was the first man to step on the Moon?", listOf("Buzz Aldrin", "Neil Armstrong", "Yuri Gagarin", "Michael Collins"), 1),
                            Quiz("q_history_5", "Which empire built the Colosseum in Rome?", listOf("Ottoman Empire", "British Empire", "Roman Empire", "Byzantine Empire"), 2),
                            Quiz("q_history_6", "Who was the first female Prime Minister of the United Kingdom?", listOf("Theresa May", "Margaret Thatcher", "Queen Elizabeth II", "Angela Merkel"), 1),
                            Quiz("q_history_7", "In which year did India gain Independence?", listOf("1942", "1945", "1947", "1950"), 2),
                            Quiz("q_history_8", "Which explorer discovered America in 1492?", listOf("Vasco da Gama", "Marco Polo", "Christopher Columbus", "Ferdinand Magellan"), 2),
                            Quiz("q_history_9", "Who was the legendary leader of the Mongol Empire?", listOf("Genghis Khan", "Kublai Khan", "Attila the Hun", "Alexander the Great"), 0),
                            Quiz("q_history_10", "Which country gifted the Statue of Liberty to the USA?", listOf("United Kingdom", "France", "Germany", "Italy"), 1),
                            Quiz("q_history_11", "What was the capital of the Byzantine Empire?", listOf("Athens", "Rome", "Constantinople", "Cairo"), 2),
                            Quiz("q_history_12", "Who wrote the play 'Romeo and Juliet'?", listOf("Mark Twain", "William Shakespeare", "Charles Dickens", "Leo Tolstoy"), 1),
                            Quiz("q_history_13", "The French Revolution began in which year?", listOf("1776", "1789", "1804", "1815"), 1),
                            Quiz("q_history_14", "Which famous queen ruled ancient Egypt?", listOf("Nefertiti", "Cleopatra", "Hatshepsut", "Zenobia"), 1),
                            Quiz("q_history_15", "Which city was the capital of the ancient Roman Empire?", listOf("Paris", "London", "Rome", "Athens"), 2)
                        ),
                        "Technology" to listOf(
                            Quiz("q_tech_1", "Who co-founded Microsoft with Paul Allen?", listOf("Steve Jobs", "Bill Gates", "Mark Zuckerberg", "Jeff Bezos"), 1),
                            Quiz("q_tech_2", "What does 'WWW' stand for?", listOf("World Wide Web", "World Wide Wrestling", "Western Washington Web", "World Wide Waves"), 0),
                            Quiz("q_tech_3", "Which company created the Android operating system?", listOf("Apple", "Microsoft", "Google", "Samsung"), 2),
                            Quiz("q_tech_4", "Which computer language is mainly used for Android (Compose) development?", listOf("Swift", "Java", "Kotlin", "Python"), 2),
                            Quiz("q_tech_5", "What is the brain of a computer?", listOf("RAM", "GPU", "Hard Drive", "CPU"), 3),
                            Quiz("q_tech_6", "Which social network was created by Mark Zuckerberg?", listOf("Twitter", "Facebook", "Instagram", "Snapchat"), 1),
                            Quiz("q_tech_7", "What is the primary function of a firewall?", listOf("Monitoring temp", "Prevent unauthorized access", "Antivirus scan", "Speed up internet"), 1),
                            Quiz("q_tech_8", "What does USB stand for?", listOf("Universal Serial Bus", "United Serial Board", "Universal Speed Booster", "Unique System Backup"), 0),
                            Quiz("q_tech_9", "In tech, what does AI stand for?", listOf("Applied Integration", "Artificial Intelligence", "Advanced Input", "Authorized Interface"), 1),
                            Quiz("q_tech_10", "Which tech company uses a bitten apple logo?", listOf("Google", "Microsoft", "Apple", "Dell"), 2),
                            Quiz("q_tech_11", "How many bits are in a byte?", listOf("4", "8", "16", "32"), 1),
                            Quiz("q_tech_12", "Which popular database is commonly used for mobile apps locally?", listOf("SQLite", "MySQL", "PostgreSQL", "Oracle"), 0),
                            Quiz("q_tech_13", "What does PDF stand for?", listOf("Public Document File", "Portable Document Format", "Personal Data Folder", "Printable Digital Form"), 1),
                            Quiz("q_tech_14", "What is the name of Amazon's cloud computing platform?", listOf("Azure", "AWS", "Google Cloud", "IBM Cloud"), 1),
                            Quiz("q_tech_15", "Who is the primary developer of the Linux kernel?", listOf("Steve Wozniak", "Bill Gates", "Linus Torvalds", "Richard Stallman"), 2)
                        )
                    )
                    for ((category, list) in pool) {
                        for (q in list) {
                            questionsRef.child(category).child(q.id).setValue(q)
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun syncUserWallet(
        userId: String, 
        coins: Int, 
        dailyStreak: Int, 
        lastCheckInTime: Long,
        remainingSpins: Int,
        totalSpinRewards: Int,
        remainingScratchCards: Int,
        lastScratchResetTime: Long,
        totalScratchRewards: Int,
        lastSpinDate: String = "",
        freeSpinUsed: Boolean = false,
        rewardAdSpinUsed: Boolean = false,
        lastCheckInDate: String = "",
        totalCheckInRewards: Int = 0,
        lastRewardAdTime: Long = 0L,
        freeScratchUsed: Boolean = false,
        rewardAdScratchUsed: Boolean = false,
        lastScratchDate: String = ""
    ) {
        if (userId.isEmpty()) return
        val updates = mapOf(
            "streak" to dailyStreak,
            "dailyStreak" to dailyStreak,
            "lastCheckInTime" to lastCheckInTime,
            "lastCheckInDate" to lastCheckInDate,
            "totalCheckInRewards" to totalCheckInRewards,
            "lastActiveTime" to System.currentTimeMillis(),
            "remainingSpins" to remainingSpins,
            "totalSpinRewards" to totalSpinRewards,
            "remainingScratchCards" to remainingScratchCards,
            "lastScratchResetTime" to lastScratchResetTime,
            "totalScratchRewards" to totalScratchRewards,
            "lastSpinDate" to lastSpinDate,
            "freeSpinUsed" to freeSpinUsed,
            "rewardAdSpinUsed" to rewardAdSpinUsed,
            "lastRewardAdTime" to lastRewardAdTime,
            "freeScratchUsed" to freeScratchUsed,
            "rewardAdScratchUsed" to rewardAdScratchUsed,
            "lastScratchDate" to lastScratchDate
        )
        usersRef.child(userId).updateChildren(updates)
    }

    fun addCoinsAtomic(userId: String, amount: Int, source: String, onComplete: (Boolean, Int, Int) -> Unit = { _, _, _ -> }) {
        if (userId.isEmpty()) {
            onComplete(false, 0, 0)
            return
        }
        val userRef = database.getReference("users").child(userId)
        userRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            var coinsBefore = 0
            var coinsAfter = 0

            override fun doTransaction(mutableData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                val currentCoins = mutableData.child("coins").getValue(Int::class.java) ?: 0
                coinsBefore = currentCoins
                coinsAfter = currentCoins + amount
                
                mutableData.child("coins").value = coinsAfter
                return com.google.firebase.database.Transaction.success(mutableData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (committed && error == null) {
                    val type = when {
                        source.contains("Daily", ignoreCase = true) -> "daily_reward"
                        source.contains("Video", ignoreCase = true) || source.contains("Ad", ignoreCase = true) -> "video_ad"
                        source.contains("Referral", ignoreCase = true) -> "referral"
                        source.contains("Spin", ignoreCase = true) -> "spin_reward"
                        source.contains("Scratch", ignoreCase = true) -> "scratch_reward"
                        source.contains("Coupon", ignoreCase = true) || source.contains("Redeem", ignoreCase = true) || amount < 0 -> "coupon_redeemed"
                        else -> "reward"
                    }

                    val txRef = database.getReference("transactions").child(userId).push()
                    val tx = FirebaseTransaction(
                        id = txRef.key ?: "",
                        userId = userId,
                        type = type,
                        title = source,
                        coins = amount,
                        status = "Completed",
                        timestamp = System.currentTimeMillis(),
                        amount = amount,
                        source = source,
                        coinsBefore = coinsBefore,
                        coinsAfter = coinsAfter
                    )
                    txRef.setValue(tx)

                    val historyRef = database.getReference("rewardHistory").child(userId).push()
                    val historyMap = mapOf(
                        "type" to type,
                        "reward" to amount,
                        "timestamp" to com.google.firebase.database.ServerValue.TIMESTAMP,
                        "coinsBefore" to coinsBefore,
                        "coinsAfter" to coinsAfter
                    )
                    historyRef.setValue(historyMap)

                    onComplete(true, coinsBefore, coinsAfter)
                } else {
                    onComplete(false, 0, 0)
                }
            }
        })
    }

    fun saveNewUserInFirebase(user: FirebaseUser) {
        val uid = if (user.uid.isNotEmpty()) user.uid else user.userId
        if (uid.isEmpty()) return
        usersRef.child(uid).setValue(user)
    }

    fun createOriginalUserInFirebase(
        uid: String,
        email: String,
        displayName: String,
        onComplete: (Boolean) -> Unit
    ) {
        if (uid.isEmpty()) {
            onComplete(false)
            return
        }
        val userMap = mapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "coins" to 0,
            "level" to 1,
            "streak" to 0,
            "records" to 0,
            "dailyRewards" to 0,
            "referrals" to 0,
            "rewardHistoryCount" to 0,
            "createdAt" to com.google.firebase.database.ServerValue.TIMESTAMP,
            "userId" to uid,
            "dailyStreak" to 0,
            "lastCheckInTime" to 0L,
            "lastActiveTime" to System.currentTimeMillis(),
            "remainingSpins" to 1,
            "totalSpinRewards" to 0,
            "remainingScratchCards" to 3,
            "lastScratchResetTime" to 0L,
            "totalScratchRewards" to 0,
            "lastRewardAdTime" to 0L
        )
        usersRef.child(uid).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
    }

    private val lastTxKeys = java.util.concurrent.ConcurrentHashMap<String, String>()

    fun addFirebaseTransaction(userId: String, amount: Int, source: String) {
        if (userId.isEmpty()) return
        
        val type = when {
            source.contains("Daily", ignoreCase = true) -> "daily_reward"
            source.contains("Video", ignoreCase = true) || source.contains("Ad", ignoreCase = true) -> "video_ad"
            source.contains("Referral", ignoreCase = true) -> "referral"
            source.contains("Spin", ignoreCase = true) -> "spin_reward"
            source.contains("Scratch", ignoreCase = true) -> "scratch_reward"
            source.contains("Coupon", ignoreCase = true) || source.contains("Redeem", ignoreCase = true) || amount < 0 -> "coupon_redeemed"
            else -> "reward"
        }
        
        val currentSec = System.currentTimeMillis() / 8000 // 8 second deduplication window
        val txKey = "$type:$amount:$currentSec"
        if (lastTxKeys[userId] == txKey) {
            return
        }
        lastTxKeys[userId] = txKey
        
        val userTxRef = database.getReference("transactions").child(userId).push()
        val txId = userTxRef.key ?: ""
        
        val tx = FirebaseTransaction(
            id = txId,
            userId = userId,
            type = type,
            title = source,
            coins = amount,
            status = "Completed",
            timestamp = System.currentTimeMillis(),
            amount = amount,
            source = source
        )
        userTxRef.setValue(tx)
    }

    fun observeTransactions(userId: String): Flow<List<FirebaseTransaction>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val userTxRef = database.getReference("transactions").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val txList = mutableListOf<FirebaseTransaction>()
                for (child in snapshot.children) {
                    val tx = child.getValue(FirebaseTransaction::class.java)
                    if (tx != null) {
                        txList.add(tx.copy(id = child.key ?: tx.id))
                    }
                }
                txList.sortByDescending { it.timestamp }
                trySend(txList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        userTxRef.addValueEventListener(listener)
        awaitClose {
            userTxRef.removeEventListener(listener)
        }
    }

    fun addFirebaseReferral(referredUserId: String, referralCode: String) {
        if (referredUserId.isEmpty()) return
        val refNode = referralsRef.push()
        val referralId = refNode.key ?: ""
        val referral = FirebaseReferral(
            id = referralId,
            referredUserId = referredUserId,
            referralCode = referralCode,
            timestamp = System.currentTimeMillis()
        )
        refNode.setValue(referral)
    }

    fun observeTasks(): Flow<List<FirebaseTask>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<FirebaseTask>()
                for (child in snapshot.children) {
                    child.getValue(FirebaseTask::class.java)?.let { list.add(it) }
                }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        tasksRef.addValueEventListener(listener)
        awaitClose { tasksRef.removeEventListener(listener) }
    }

    fun observeCoupons(): Flow<List<FirebaseCoupon>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<FirebaseCoupon>()
                for (child in snapshot.children) {
                    child.getValue(FirebaseCoupon::class.java)?.let { list.add(it) }
                }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        couponsRef.addValueEventListener(listener)
        awaitClose { couponsRef.removeEventListener(listener) }
    }

    fun observeFirebaseUser(userId: String): Flow<FirebaseUser?> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val userRef = usersRef.child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(FirebaseUser::class.java)
                trySend(user)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        userRef.addValueEventListener(listener)
        awaitClose { userRef.removeEventListener(listener) }
    }

    suspend fun getFirebaseUser(userId: String): FirebaseUser? = kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        if (userId.isEmpty()) {
            continuation.resume(null) {}
            return@suspendCancellableCoroutine
        }
        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(FirebaseUser::class.java)
                continuation.resume(user) {}
            }
            override fun onCancelled(error: DatabaseError) {
                continuation.resume(null) {}
            }
        })
    }

    fun resetUserDataInFirebase(userId: String) {
        if (userId.isEmpty()) return
        
        // 1. Reset user state in users Ref (preserving email, displayName, uid, joinedAt by doing a partial update)
        val updates = mapOf(
            "coins" to 0,
            "level" to 1,
            "streak" to 0,
            "dailyStreak" to 0,
            "totalReferrals" to 0,
            "rewardHistoryCount" to 0,
            "lastCheckInTime" to 0L,
            "lastActiveTime" to System.currentTimeMillis(),
            "remainingSpins" to 1,
            "totalSpinRewards" to 0,
            "remainingScratchCards" to 3,
            "lastScratchResetTime" to 0L,
            "totalScratchRewards" to 0,
            "lastSpinDate" to "",
            "freeSpinUsed" to false,
            "rewardAdSpinUsed" to false,
            "lastRewardAdTime" to 0L
        )
        usersRef.child(userId).updateChildren(updates)

        // 2. Delete transactions for this user
        database.getReference("transactions").child(userId).removeValue()

        // 3. Delete referrals for this user
        referralsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val ref = child.getValue(FirebaseReferral::class.java)
                    if (ref != null && ref.referredUserId == userId) {
                        child.ref.removeValue()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun observeRedemptions(userId: String): Flow<List<FirebaseRedemption>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val userRedemptionsRef = database.getReference("redemptions").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<FirebaseRedemption>()
                for (child in snapshot.children) {
                    child.getValue(FirebaseRedemption::class.java)?.let { list.add(it) }
                }
                list.sortByDescending { it.timestamp }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        userRedemptionsRef.addValueEventListener(listener)
        awaitClose { userRedemptionsRef.removeEventListener(listener) }
    }

    fun addFirebaseRedemption(userId: String, redemption: FirebaseRedemption) {
        if (userId.isEmpty() || redemption.id.isEmpty()) return
        database.getReference("redemptions").child(userId).child(redemption.id).setValue(redemption)
    }

    fun updateFirebaseRedemptionStatus(userId: String, redemptionId: String, status: String) {
        if (userId.isEmpty() || redemptionId.isEmpty()) return
        database.getReference("redemptions").child(userId).child(redemptionId).child("status").setValue(status)
    }

    fun addFirebaseCoupon(coupon: FirebaseCoupon) {
        val id = if (coupon.id.isEmpty()) couponsRef.push().key ?: "" else coupon.id
        val finalCoupon = coupon.copy(id = id)
        couponsRef.child(finalCoupon.id).setValue(finalCoupon)
    }

    fun deleteFirebaseCoupon(couponId: String) {
        if (couponId.isEmpty()) return
        couponsRef.child(couponId).removeValue()
    }

    // --- UPI WITHDRAWAL SYSTEM OPERATIONS ---

    fun submitWithdrawRequest(
        uid: String,
        userName: String,
        email: String,
        upiId: String,
        amount: Int,
        coinsSpent: Int,
        onComplete: (Boolean, String?) -> Unit
    ) {
        if (uid.isEmpty()) {
            onComplete(false, "Authentication required.")
            return
        }

        val userCoinsRef = database.getReference("users").child(uid).child("coins")
        userCoinsRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                val currentCoins = currentData.getValue(Int::class.java) ?: 0
                if (currentCoins < coinsSpent) {
                    return com.google.firebase.database.Transaction.abort()
                }
                currentData.value = currentCoins - coinsSpent
                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (committed && error == null) {
                    val withdrawRef = database.getReference("withdrawRequests").push()
                    val reqId = withdrawRef.key ?: ""
                    
                    val txRef = database.getReference("transactions").child(uid).push()
                    val txId = txRef.key ?: ""
                    
                    val request = com.playwin.app.data.model.FirebaseWithdrawRequest(
                        id = reqId,
                        uid = uid,
                        userName = userName,
                        email = email,
                        upiId = upiId,
                        amount = amount,
                        coinsSpent = coinsSpent,
                        status = "Pending",
                        timestamp = System.currentTimeMillis(),
                        transactionId = txId
                    )
                    
                    val tx = FirebaseTransaction(
                        id = txId,
                        userId = uid,
                        type = "withdraw",
                        title = "Withdraw Request",
                        coins = -coinsSpent,
                        status = "Pending",
                        timestamp = System.currentTimeMillis()
                    )
                    
                    txRef.setValue(tx)
                    withdrawRef.setValue(request)
                    
                    onComplete(true, null)
                } else {
                    onComplete(false, error?.message ?: "Insufficient Coins or database error.")
                }
            }
        })
    }

    fun approveWithdrawRequest(
        requestId: String,
        uid: String,
        transactionId: String,
        remarks: String,
        onComplete: (Boolean) -> Unit
    ) {
        if (requestId.isEmpty()) {
            onComplete(false)
            return
        }
        val updates = mapOf(
            "status" to "Approved",
            "remarks" to remarks
        )
        database.getReference("withdrawRequests").child(requestId).updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful && transactionId.isNotEmpty()) {
                database.getReference("transactions").child(uid).child(transactionId).child("status").setValue("Approved")
            }
            onComplete(task.isSuccessful)
        }
    }

    fun rejectWithdrawRequest(
        requestId: String,
        uid: String,
        transactionId: String,
        coinsSpent: Int,
        remarks: String,
        onComplete: (Boolean) -> Unit
    ) {
        if (requestId.isEmpty()) {
            onComplete(false)
            return
        }
        
        val userCoinsRef = database.getReference("users").child(uid).child("coins")
        userCoinsRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                val currentCoins = currentData.getValue(Int::class.java) ?: 0
                currentData.value = currentCoins + coinsSpent
                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (committed && error == null) {
                    val updates = mapOf(
                        "status" to "Rejected",
                        "remarks" to remarks
                    )
                    database.getReference("withdrawRequests").child(requestId).updateChildren(updates)
                    
                    if (transactionId.isNotEmpty()) {
                        database.getReference("transactions").child(uid).child(transactionId).child("status").setValue("Rejected")
                    }
                    
                    val refundTxRef = database.getReference("transactions").child(uid).push()
                    val refundTx = FirebaseTransaction(
                        id = refundTxRef.key ?: "",
                        userId = uid,
                        type = "refund",
                        title = "Withdraw Void Refund",
                        coins = coinsSpent,
                        status = "Completed",
                        timestamp = System.currentTimeMillis()
                    )
                    refundTxRef.setValue(refundTx)
                    
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
        })
    }

    fun setBlockUserStatus(userId: String, isBlocked: Boolean, onComplete: (Boolean) -> Unit) {
        if (userId.isEmpty()) {
            onComplete(false)
            return
        }
        database.getReference("users").child(userId).child("isBlocked").setValue(isBlocked).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

    fun modifyUserCoinsManually(userId: String, amount: Int, description: String, onComplete: (Boolean) -> Unit) {
        if (userId.isEmpty()) {
            onComplete(false)
            return
        }
        val userCoinsRef = database.getReference("users").child(userId).child("coins")
        userCoinsRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                val currentCoins = currentData.getValue(Int::class.java) ?: 0
                currentData.value = currentCoins + amount
                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (committed && error == null) {
                    val changeTxRef = database.getReference("transactions").child(userId).push()
                    val changeTx = FirebaseTransaction(
                        id = changeTxRef.key ?: "",
                        userId = userId,
                        type = if (amount >= 0) "manual_add" else "manual_remove",
                        title = description,
                        coins = amount,
                        status = "Completed",
                        timestamp = System.currentTimeMillis()
                    )
                    changeTxRef.setValue(changeTx)
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
        })
    }

    fun observeWithdrawRequests(userId: String): kotlinx.coroutines.flow.Flow<List<com.playwin.app.data.model.FirebaseWithdrawRequest>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val ref = database.getReference("withdrawRequests")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<com.playwin.app.data.model.FirebaseWithdrawRequest>()
                for (child in snapshot.children) {
                    val req = child.getValue(com.playwin.app.data.model.FirebaseWithdrawRequest::class.java)
                    if (req != null && req.uid == userId) {
                        list.add(req)
                    }
                }
                list.sortByDescending { it.timestamp }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeAllWithdrawRequests(): kotlinx.coroutines.flow.Flow<List<com.playwin.app.data.model.FirebaseWithdrawRequest>> = callbackFlow {
        val ref = database.getReference("withdrawRequests")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<com.playwin.app.data.model.FirebaseWithdrawRequest>()
                for (child in snapshot.children) {
                    child.getValue(com.playwin.app.data.model.FirebaseWithdrawRequest::class.java)?.let { list.add(it) }
                }
                list.sortByDescending { it.timestamp }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeAllUsers(): kotlinx.coroutines.flow.Flow<List<FirebaseUser>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<FirebaseUser>()
                for (child in snapshot.children) {
                    try {
                        child.getValue(FirebaseUser::class.java)?.let { list.add(it) }
                    } catch (e: Exception) {
                        // Safe ignore malformed entry
                    }
                }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        usersRef.addValueEventListener(listener)
        awaitClose { usersRef.removeEventListener(listener) }
    }

    fun observeAllReferrals(): kotlinx.coroutines.flow.Flow<List<FirebaseReferral>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<FirebaseReferral>()
                for (child in snapshot.children) {
                    try {
                        child.getValue(FirebaseReferral::class.java)?.let { list.add(it) }
                    } catch (e: Exception) {
                        // Safe skip malformed entries/strings
                    }
                }
                list.sortByDescending { it.timestamp }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        referralsRef.addValueEventListener(listener)
        awaitClose { referralsRef.removeEventListener(listener) }
    }

    fun observeAllRedemptions(): kotlinx.coroutines.flow.Flow<List<com.playwin.app.data.model.FirebaseRedemption>> = callbackFlow {
        val ref = database.getReference("redemptions")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<com.playwin.app.data.model.FirebaseRedemption>()
                for (userNode in snapshot.children) {
                    for (redemptionNode in userNode.children) {
                        try {
                            redemptionNode.getValue(com.playwin.app.data.model.FirebaseRedemption::class.java)?.let {
                                list.add(it)
                            }
                        } catch (e: Exception) {
                            // Safe skip malformed entries
                        }
                    }
                }
                list.sortByDescending { it.timestamp }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeAllTransactions(): kotlinx.coroutines.flow.Flow<Map<String, List<FirebaseTransaction>>> = callbackFlow {
        val ref = database.getReference("transactions")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = mutableMapOf<String, List<FirebaseTransaction>>()
                for (userNode in snapshot.children) {
                    val userId = userNode.key ?: continue
                    val list = mutableListOf<FirebaseTransaction>()
                    for (child in userNode.children) {
                        try {
                            child.getValue(FirebaseTransaction::class.java)?.let { list.add(it) }
                        } catch (e: Exception) {
                            // Safe skip malformed entries
                        }
                    }
                    list.sortByDescending { it.timestamp }
                    map[userId] = list
                }
                trySend(map)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeQuizProgress(userId: String): kotlinx.coroutines.flow.Flow<com.playwin.app.data.model.FirebaseQuizProgress?> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val ref = database.getReference("users/$userId/quizProgress")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val progress = try {
                    snapshot.getValue(com.playwin.app.data.model.FirebaseQuizProgress::class.java)
                } catch (e: Exception) {
                    null
                }
                trySend(progress)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun saveQuizProgress(userId: String, progress: com.playwin.app.data.model.FirebaseQuizProgress) {
        if (userId.isEmpty()) return
        database.getReference("users/$userId/quizProgress").setValue(progress)
    }

    fun getQuestionsForCategory(category: String, onResult: (List<com.playwin.app.data.model.Quiz>) -> Unit) {
        val ref = database.getReference("questions/$category")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val questions = mutableListOf<com.playwin.app.data.model.Quiz>()
                for (child in snapshot.children) {
                    try {
                        val q = child.getValue(com.playwin.app.data.model.Quiz::class.java)
                        if (q != null) {
                            questions.add(q)
                        }
                    } catch (e: Exception) {
                        // ignore malformed
                    }
                }
                onResult(questions)
            }
            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }
        })
    }

    fun observeAllCouponRedemptions(): kotlinx.coroutines.flow.Flow<List<com.playwin.app.data.model.FirebaseCouponRedemption>> = callbackFlow {
        val ref = database.getReference("couponRedemptions")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<com.playwin.app.data.model.FirebaseCouponRedemption>()
                for (child in snapshot.children) {
                    try {
                        child.getValue(com.playwin.app.data.model.FirebaseCouponRedemption::class.java)?.let {
                            list.add(it)
                        }
                    } catch (e: Exception) {
                        // Safe skip malformed entries
                    }
                }
                list.sortByDescending { it.createdAt }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeUserCouponRedemptions(userId: String): kotlinx.coroutines.flow.Flow<List<com.playwin.app.data.model.FirebaseCouponRedemption>> = callbackFlow {
        val ref = database.getReference("couponRedemptions")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<com.playwin.app.data.model.FirebaseCouponRedemption>()
                for (child in snapshot.children) {
                    try {
                        val r = child.getValue(com.playwin.app.data.model.FirebaseCouponRedemption::class.java)
                        if (r != null && r.userUid == userId) {
                            list.add(r)
                        }
                    } catch (e: Exception) {
                        // Safe skip malformed entries
                    }
                }
                list.sortByDescending { it.createdAt }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun submitCouponRedemptionTransaction(
        redemption: com.playwin.app.data.model.FirebaseCouponRedemption,
        couponId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val rootRef = database.reference
        rootRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                val userId = redemption.userUid
                val requestId = redemption.requestId
                val couponName = redemption.couponName
                val requiredCoins = redemption.requiredCoins

                // 1. Check user coins
                val userCoinsVal = currentData.child("users").child(userId).child("coins").getValue(Int::class.java) ?: 0
                if (userCoinsVal < requiredCoins) {
                    return com.google.firebase.database.Transaction.abort()
                }

                // 2. Check coupon stock
                val couponStockVal = currentData.child("coupons").child(couponId).child("stock").getValue(Int::class.java) ?: 0
                if (couponStockVal <= 0) {
                    return com.google.firebase.database.Transaction.abort()
                }

                // 3. Deduct coins from user
                currentData.child("users").child(userId).child("coins").value = userCoinsVal - requiredCoins

                // 4. Reduce coupon stock by 1
                currentData.child("coupons").child(couponId).child("stock").value = couponStockVal - 1

                // 5. Create redemption request under couponRedemptions/{requestId}
                val serverTime = System.currentTimeMillis()
                val redemptionMap = mapOf(
                    "requestId" to requestId,
                    "userUid" to userId,
                    "displayName" to redemption.displayName,
                    "email" to redemption.email,
                    "mobileNumber" to redemption.mobileNumber,
                    "couponName" to couponName,
                    "requiredCoins" to requiredCoins,
                    "giftCardOrRechargeNumber" to redemption.giftCardOrRechargeNumber,
                    "additionalNotes" to redemption.additionalNotes,
                    "status" to "Pending",
                    "createdAt" to serverTime
                )
                currentData.child("couponRedemptions").child(requestId).value = redemptionMap

                // 6. Create legacy / standard redemption history under redemptions/{userId}/{requestId}
                val legacyRedemptionMap = mapOf(
                    "id" to requestId,
                    "userId" to userId,
                    "couponId" to couponId,
                    "couponName" to couponName,
                    "coinsSpent" to requiredCoins,
                    "status" to "Pending",
                    "timestamp" to serverTime,
                    "couponCode" to "Pending Approval",
                    "expiryDate" to "N/A"
                )
                currentData.child("redemptions").child(userId).child(requestId).value = legacyRedemptionMap

                // 7. Create transaction history under transactions/{userId}/{txId}
                val txId = "tx_red_" + serverTime + "_" + (1000..9999).random()
                val txMap = mapOf(
                    "id" to txId,
                    "userId" to userId,
                    "type" to "coupon_redeemed",
                    "title" to "Redeemed $couponName",
                    "coins" to -requiredCoins,
                    "status" to "Completed",
                    "timestamp" to serverTime,
                    "amount" to -requiredCoins,
                    "source" to "Coupon Store",
                    "coinsBefore" to userCoinsVal,
                    "coinsAfter" to (userCoinsVal - requiredCoins)
                )
                currentData.child("transactions").child(userId).child(txId).value = txMap

                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(
                error: com.google.firebase.database.DatabaseError?,
                committed: Boolean,
                currentData: com.google.firebase.database.DataSnapshot?
            ) {
                if (error != null) {
                    onError(error.message)
                } else if (!committed) {
                    onError("Transaction aborted. Either Insufficient Coins or Coupon Out of Stock.")
                } else {
                    onSuccess()
                }
            }
        })
    }

    fun updateCouponRedemptionStatus(requestId: String, userUid: String, status: String) {
        if (requestId.isEmpty()) return
        database.getReference("couponRedemptions").child(requestId).child("status").setValue(status)
        if (userUid.isNotEmpty()) {
            database.getReference("redemptions").child(userUid).child(requestId).child("status").setValue(status)
        }
    }
}

