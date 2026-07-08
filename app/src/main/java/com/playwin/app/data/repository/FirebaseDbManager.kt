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
    private val database: FirebaseDatabase by lazy {
        try {
            val db = FirebaseDatabase.getInstance("https://play-win-e01bc-default-rtdb.asia-southeast1.firebasedatabase.app")
            try {
                db.setPersistenceEnabled(true)
            } catch (e: Exception) {
                // ignore
            }
            db
        } catch (e: Exception) {
            android.util.Log.e("FirebaseDbManager", "Failed to initialize FirebaseDatabase: ${e.message}", e)
            throw e
        }
    }
    private val tasksRef by lazy { database.getReference("tasks") }
    private val couponsRef by lazy { database.getReference("coupons") }
    private val usersRef by lazy { database.getReference("users") }
    private val transactionsRef by lazy { database.getReference("transactions") }
    private val referralsRef by lazy { database.getReference("referrals") }

    init {
        try {
            seedInitialDataIfNeeded()
        } catch (e: Exception) {
            android.util.Log.e("FirebaseDbManager", "Error in init seeding: ${e.message}", e)
        }
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
                            couponId = "coupon_amazon",
                            couponName = "Amazon Gift Card",
                            coinCost = 100,
                            requiredCoins = 100,
                            code = "AMZ-PW-100",
                            description = "Redeem for Amazon Pay Gift Card. Use for shopping, bills, and payments.",
                            category = "Shopping",
                            status = "In Stock",
                            expiryDate = "31 Dec 2026",
                            couponImage = "🛍️",
                            remainingStock = 50
                        ),
                        FirebaseCoupon(
                            couponId = "coupon_flipkart",
                            couponName = "Flipkart Gift Card",
                            coinCost = 200,
                            requiredCoins = 200,
                            code = "FK-PW-200",
                            description = "Redeem for Flipkart Gift Card. Shop from millions of items with free delivery options.",
                            category = "Shopping",
                            status = "In Stock",
                            expiryDate = "31 Dec 2026",
                            couponImage = "🛒",
                            remainingStock = 40
                        ),
                        FirebaseCoupon(
                            couponId = "coupon_google",
                            couponName = "Google Play Gift Card",
                            coinCost = 150,
                            requiredCoins = 150,
                            code = "GP-PW-150",
                            description = "Get credits in Google Play Store. Buy premium apps, books, movies, or in-app items.",
                            category = "Shopping",
                            status = "In Stock",
                            expiryDate = "31 Dec 2026",
                            couponImage = "🎮",
                            remainingStock = 30
                        ),
                        FirebaseCoupon(
                            couponId = "coupon_jiohotstar",
                            couponName = "JioHotstar",
                            coinCost = 500,
                            requiredCoins = 500,
                            code = "JH-PW-500",
                            description = "Enjoy premium movies, sports, and TV shows with JioHotstar subscription.",
                            category = "OTT",
                            status = "In Stock",
                            expiryDate = "31 Dec 2026",
                            couponImage = "🎬",
                            remainingStock = 15
                        ),
                        FirebaseCoupon(
                            couponId = "coupon_netflix",
                            couponName = "Netflix",
                            coinCost = 800,
                            requiredCoins = 800,
                            code = "NF-PW-800",
                            description = "Unlock Netflix Premium subscription for top-tier films and web series.",
                            category = "OTT",
                            status = "In Stock",
                            expiryDate = "31 Dec 2026",
                            couponImage = "🍿",
                            remainingStock = 10
                        ),
                        FirebaseCoupon(
                            couponId = "coupon_prime",
                            couponName = "Amazon Prime",
                            coinCost = 600,
                            requiredCoins = 600,
                            code = "AP-PW-600",
                            description = "Get free fast delivery, Prime Video, Prime Music, and exclusive deals.",
                            category = "OTT",
                            status = "In Stock",
                            expiryDate = "31 Dec 2026",
                            couponImage = "👑",
                            remainingStock = 20
                        ),
                        FirebaseCoupon(
                            couponId = "coupon_sonyliv",
                            couponName = "Sony LIV",
                            coinCost = 400,
                            requiredCoins = 400,
                            code = "SL-PW-400",
                            description = "Watch exclusive sports events, original shows, and movies on Sony LIV.",
                            category = "OTT",
                            status = "In Stock",
                            expiryDate = "31 Dec 2026",
                            couponImage = "📺",
                            remainingStock = 25
                        ),
                        FirebaseCoupon(
                            couponId = "coupon_zee5",
                            couponName = "ZEE5",
                            coinCost = 300,
                            requiredCoins = 300,
                            code = "Z5-PW-300",
                            description = "Get access to unlimited blockbuster movies and exclusive web originals.",
                            category = "OTT",
                            status = "In Stock",
                            expiryDate = "31 Dec 2026",
                            couponImage = "📱",
                            remainingStock = 35
                        )
                    )
                    for (coupon in initialCoupons) {
                        couponsRef.child(coupon.couponId).setValue(coupon)
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

        val spinWheelRewardsRef = database.getReference("spinWheelRewards")
        spinWheelRewardsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                    val initialSpinRewards = listOf(
                        com.playwin.app.data.model.FirebaseSpinReward(id = "spin_1", name = "+5 Coins", type = "Coins", value = "5", displayOrder = 1, probabilityWeight = 35, active = true),
                        com.playwin.app.data.model.FirebaseSpinReward(id = "spin_2", name = "+10 Coins", type = "Coins", value = "10", displayOrder = 2, probabilityWeight = 25, active = true),
                        com.playwin.app.data.model.FirebaseSpinReward(id = "spin_3", name = "+20 Coins", type = "Coins", value = "20", displayOrder = 3, probabilityWeight = 20, active = true),
                        com.playwin.app.data.model.FirebaseSpinReward(id = "spin_4", name = "Better Luck", type = "Better Luck Next Time", value = "0", displayOrder = 4, probabilityWeight = 10, active = true),
                        com.playwin.app.data.model.FirebaseSpinReward(id = "spin_5", name = "Retry Spin", type = "Retry", value = "1", displayOrder = 5, probabilityWeight = 7, active = true),
                        com.playwin.app.data.model.FirebaseSpinReward(id = "spin_6", name = "Amazon Voucher", type = "Coupon", value = "coupon_amazon", displayOrder = 6, probabilityWeight = 2, active = true),
                        com.playwin.app.data.model.FirebaseSpinReward(id = "spin_7", name = "+50 Coins", type = "Coins", value = "50", displayOrder = 7, probabilityWeight = 1, active = true)
                    )
                    for (reward in initialSpinRewards) {
                        spinWheelRewardsRef.child(reward.id).setValue(reward)
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
        dailySpinCount: Int = 0,
        rewardedSpinCount: Int = 0,
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
            "dailySpinCount" to dailySpinCount,
            "rewardedSpinCount" to rewardedSpinCount,
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
        val type = when {
            source.contains("Daily", ignoreCase = true) -> "daily_reward"
            source.contains("Video", ignoreCase = true) || source.contains("Ad", ignoreCase = true) -> "video_ad"
            source.contains("Referral", ignoreCase = true) -> "referral"
            source.contains("Spin", ignoreCase = true) -> "spin_reward"
            source.contains("Scratch", ignoreCase = true) -> "scratch_reward"
            source.contains("Coupon", ignoreCase = true) || source.contains("Redeem", ignoreCase = true) || amount < 0 -> "coupon_redeemed"
            else -> "reward"
        }
        WalletService.updateWallet(
            userId = userId,
            coinsDelta = amount,
            source = source,
            type = type,
            extraCheck = { null },
            extraUpdate = { null },
            onComplete = { success, coinsBefore, coinsAfter, _ ->
                onComplete(success, coinsBefore, coinsAfter)
            }
        )
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
                val dbPath = "coupons"
                
                android.util.Log.d("FirebaseDbManager", "observeCoupons: checking path '$dbPath'")
                
                if (!snapshot.exists()) {
                    android.util.Log.w("FirebaseDbManager", "observeCoupons: Path '$dbPath' does NOT exist in Firebase.")
                    trySend(emptyList())
                    return
                }
                
                for (child in snapshot.children) {
                    val childKey = child.key ?: "unknown_key"
                    try {
                        val couponIdVal = child.child("couponId").value ?: child.child("id").value
                        var couponId = couponIdVal?.toString() ?: ""
                        if (couponId.isBlank()) {
                            couponId = childKey
                        }
                            
                        val couponNameVal = child.child("couponName").value ?: child.child("title").value
                        val couponName = couponNameVal?.toString() ?: ""
                        
                        val requiredCoinsVal = child.child("requiredCoins").value
                        val coinCostVal = child.child("coinCost").value
                        val costVal = child.child("cost").value
                        val finalCoins = when {
                            requiredCoinsVal != null -> when (requiredCoinsVal) {
                                is Number -> requiredCoinsVal.toInt()
                                is String -> requiredCoinsVal.toIntOrNull() ?: 0
                                else -> requiredCoinsVal.toString().toIntOrNull() ?: 0
                            }
                            coinCostVal != null -> when (coinCostVal) {
                                is Number -> coinCostVal.toInt()
                                is String -> coinCostVal.toIntOrNull() ?: 0
                                else -> coinCostVal.toString().toIntOrNull() ?: 0
                            }
                            costVal != null -> when (costVal) {
                                is Number -> costVal.toInt()
                                is String -> costVal.toIntOrNull() ?: 0
                                else -> costVal.toString().toIntOrNull() ?: 0
                            }
                            else -> 0
                        }
                        
                        val couponImageVal = child.child("couponImage").value ?: child.child("image").value
                        val couponImage = couponImageVal?.toString() ?: ""
                            
                        val enabledVal = child.child("enabled").value
                        val isEnabledVal = child.child("isEnabled").value
                        val finalEnabled = when {
                            enabledVal != null -> when (enabledVal) {
                                is Boolean -> enabledVal
                                is String -> enabledVal.toBoolean()
                                is Number -> enabledVal.toInt() != 0
                                else -> enabledVal.toString().toBoolean()
                            }
                            isEnabledVal != null -> when (isEnabledVal) {
                                is Boolean -> isEnabledVal
                                is String -> isEnabledVal.toBoolean()
                                is Number -> isEnabledVal.toInt() != 0
                                else -> isEnabledVal.toString().toBoolean()
                            }
                            else -> true
                        }
                        
                        val statusVal = child.child("status").value ?: child.child("availability").value
                        val status = statusVal?.toString() ?: "In Stock"
                            
                        val remainingStockVal = child.child("remainingStock").value
                        val stockVal = child.child("stock").value
                        val finalStock = when {
                            remainingStockVal != null -> when (remainingStockVal) {
                                is Number -> remainingStockVal.toInt()
                                is String -> remainingStockVal.toIntOrNull() ?: 50
                                else -> remainingStockVal.toString().toIntOrNull() ?: 50
                            }
                            stockVal != null -> when (stockVal) {
                                is Number -> stockVal.toInt()
                                is String -> stockVal.toIntOrNull() ?: 50
                                else -> stockVal.toString().toIntOrNull() ?: 50
                            }
                            else -> 50
                        }
                        
                        val brandVal = child.child("brand").value
                        val brand = brandVal?.toString() ?: ""
                        
                        val featuredVal = child.child("featured").value
                        val finalFeatured = when {
                            featuredVal is Boolean -> featuredVal
                            featuredVal is String -> featuredVal.toBoolean()
                            featuredVal is Number -> featuredVal.toInt() != 0
                            else -> featuredVal?.toString()?.toBoolean() ?: false
                        }
                        
                        val categoryVal = child.child("category").value
                        val category = categoryVal?.toString() ?: "Shopping"
                        
                        val codeVal = child.child("code").value
                        val code = codeVal?.toString() ?: ""
                        
                        val descriptionVal = child.child("description").value
                        val description = descriptionVal?.toString() ?: ""
                        
                        val expiryDateVal = child.child("expiryDate").value
                        val expiryDate = when (expiryDateVal) {
                            is Long -> {
                                val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                                sdf.format(java.util.Date(expiryDateVal))
                            }
                            is Number -> {
                                val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                                sdf.format(java.util.Date(expiryDateVal.toLong()))
                            }
                            else -> expiryDateVal?.toString() ?: ""
                        }
                        
                        val coupon = FirebaseCoupon(
                            couponId = couponId,
                            couponName = couponName,
                            coinCost = finalCoins,
                            requiredCoins = finalCoins,
                            couponImage = couponImage,
                            enabled = finalEnabled,
                            status = status,
                            remainingStock = finalStock,
                            brand = brand,
                            featured = finalFeatured,
                            category = category,
                            code = code,
                            description = description,
                            expiryDate = expiryDate
                        )
                        list.add(coupon)
                    } catch (e: Exception) {
                        android.util.Log.e(
                            "FirebaseDbManager",
                            "CRITICAL BUG: DatabaseException inside onDataChange() at path: $dbPath, key: $childKey. Full Exception: ${e.message}",
                            e
                        )
                    }
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                val dbPath = "coupons"
                android.util.Log.e("FirebaseDbManager", "observeCoupons cancelled or failed. Path: $dbPath. Error: ${error.message}", error.toException())
                trySend(emptyList())
            }
        }
        couponsRef.addValueEventListener(listener)
        awaitClose { couponsRef.removeEventListener(listener) }
    }

    fun observeSpinRewards(): Flow<List<com.playwin.app.data.model.FirebaseSpinReward>> = callbackFlow {
        android.util.Log.d("PlayWinDebug", "Firebase connected")
        android.util.Log.d("PlayWinDebug", "Reading configs/spinWheel")
        android.util.Log.d("PlayWinDebug", "Reading game_config/spinWheel")
        android.util.Log.d("PlayWinDebug", "Reading wheel_segments")

        val configRef = database.getReference("game_config/spinWheel")
        val segmentsRef = database.getReference("wheel_segments")

        var latestConfigSnapshot: DataSnapshot? = null
        var latestSegmentsSnapshot: DataSnapshot? = null

        fun sendUpdatedRewards() {
            try {
                android.util.Log.d("PlayWinDebug", "Realtime update received")
                
                val segmentsSnapshot = latestSegmentsSnapshot
                    ?: latestConfigSnapshot?.child("wheel_segments")?.takeIf { it.exists() }
                    ?: latestConfigSnapshot?.child("wheelRewards")?.takeIf { it.exists() }
                    ?: latestConfigSnapshot?.child("segments")

                val segments = if (segmentsSnapshot != null && segmentsSnapshot.exists()) {
                    parseSegmentsFromSnapshot(segmentsSnapshot)
                } else if (latestConfigSnapshot != null && latestConfigSnapshot!!.exists()) {
                    parseSegmentsFromSnapshot(latestConfigSnapshot!!)
                } else {
                    emptyList()
                }

                val enabledSegments = segments.filter { it.active || it.enabled }.sortedBy { it.order }
                
                android.util.Log.d("PlayWinDebug", "wheelRewards count: ${enabledSegments.size}")
                android.util.Log.d("PlayWinDebug", "Reward list updated")
                
                trySend(enabledSegments)
            } catch (e: Exception) {
                android.util.Log.e("FirebaseDbManager", "Error parsing spin rewards: ${e.message}", e)
                trySend(emptyList())
            }
        }

        val configListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                latestConfigSnapshot = snapshot
                sendUpdatedRewards()
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("FirebaseDbManager", "observeSpinRewards config cancelled. Code: ${error.code}.", error.toException())
            }
        }

        val segmentsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    latestSegmentsSnapshot = snapshot
                    sendUpdatedRewards()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("FirebaseDbManager", "observeSpinRewards segments cancelled. Code: ${error.code}.", error.toException())
            }
        }

        configRef.addValueEventListener(configListener)
        segmentsRef.addValueEventListener(segmentsListener)

        awaitClose {
            configRef.removeEventListener(configListener)
            segmentsRef.removeEventListener(segmentsListener)
        }
    }

    fun observeSpinWheelConfig(): Flow<com.playwin.app.data.model.FirebaseSpinWheelConfig> = callbackFlow {
        android.util.Log.d("PlayWinDebug", "Firebase connected")
        android.util.Log.d("PlayWinDebug", "Reading configs/spinWheel")
        android.util.Log.d("PlayWinDebug", "Reading game_config/spinWheel")
        android.util.Log.d("PlayWinDebug", "Reading wheel_segments")

        val configRef = database.getReference("game_config/spinWheel")
        val segmentsRef = database.getReference("wheel_segments")

        var latestConfigSnapshot: DataSnapshot? = null
        var latestSegmentsSnapshot: DataSnapshot? = null

        fun sendUpdatedConfig() {
            try {
                android.util.Log.d("PlayWinDebug", "Realtime update received")
                
                val configSnap = latestConfigSnapshot ?: return
                val segmentsSnap = latestSegmentsSnapshot

                val enabledVal = configSnap.child("enabled").value ?: configSnap.child("active").value ?: true
                val enabled = when (enabledVal) {
                    is Boolean -> enabledVal
                    is String -> enabledVal.equals("true", ignoreCase = true)
                    is Number -> enabledVal.toInt() != 0
                    else -> true
                }

                val dailySpinsVal = configSnap.child("dailySpins").value ?: configSnap.child("spinsCount").value ?: 2
                val dailySpins = when (dailySpinsVal) {
                    is Number -> dailySpinsVal.toInt()
                    is String -> dailySpinsVal.toIntOrNull() ?: 2
                    else -> 2
                }

                val title = configSnap.child("title").getValue(String::class.java) ?: "Lucky Spin & Win"

                val dailySpinLimit = when (val v = configSnap.child("dailySpinLimit").value ?: configSnap.child("dailyLimit").value) {
                    is Number -> v.toInt()
                    is String -> v.toIntOrNull() ?: 10
                    else -> 10
                }

                val dailyFreeSpins = when (val v = configSnap.child("dailyFreeSpins").value ?: configSnap.child("dailySpins").value ?: configSnap.child("spinsCount").value) {
                    is Number -> v.toInt()
                    is String -> v.toIntOrNull() ?: 2
                    else -> 2
                }

                val rewardedAdAfterFreeSpins = when (val v = configSnap.child("rewardedAdAfterFreeSpins").value) {
                    is Boolean -> v
                    is String -> v.equals("true", ignoreCase = true)
                    is Number -> v.toInt() != 0
                    else -> true
                }

                val requireRewardedAdBeforeEveryExtraSpin = when (val v = configSnap.child("requireRewardedAdBeforeEveryExtraSpin").value) {
                    is Boolean -> v
                    is String -> v.equals("true", ignoreCase = true)
                    is Number -> v.toInt() != 0
                    else -> true
                }

                val maxRewardedAdSpinsPerDay = when (val v = configSnap.child("maxRewardedAdSpinsPerDay").value) {
                    is Number -> v.toInt()
                    is String -> v.toIntOrNull() ?: 10
                    else -> 10
                }

                val lastUpdatedVal = configSnap.child("lastUpdated").value
                val lastUpdated = when (lastUpdatedVal) {
                    is Number -> lastUpdatedVal.toLong()
                    is String -> lastUpdatedVal.toLongOrNull() ?: 0L
                    else -> 0L
                }

                val segmentsSnapshot = segmentsSnap ?: configSnap.child("wheel_segments").takeIf { it.exists() }
                    ?: configSnap.child("wheelRewards").takeIf { it.exists() }
                    ?: configSnap.child("segments")

                val segments = if (segmentsSnapshot != null && segmentsSnapshot.exists()) {
                    parseSegmentsFromSnapshot(segmentsSnapshot)
                } else {
                    parseSegmentsFromSnapshot(configSnap)
                }

                android.util.Log.d("PlayWinDebug", "Config loaded: title=$title, enabled=$enabled, dailyFreeSpins=$dailyFreeSpins, dailySpinLimit=$dailySpinLimit, maxRewardedAdSpinsPerDay=$maxRewardedAdSpinsPerDay, lastUpdated=$lastUpdated")
                android.util.Log.d("PlayWinDebug", "wheelRewards count: ${segments.size}")

                val config = com.playwin.app.data.model.FirebaseSpinWheelConfig(
                    enabled = enabled,
                    dailySpins = dailySpins,
                    title = title,
                    dailySpinLimit = dailySpinLimit,
                    dailyFreeSpins = dailyFreeSpins,
                    rewardedAdAfterFreeSpins = rewardedAdAfterFreeSpins,
                    requireRewardedAdBeforeEveryExtraSpin = requireRewardedAdBeforeEveryExtraSpin,
                    maxRewardedAdSpinsPerDay = maxRewardedAdSpinsPerDay,
                    segments = segments,
                    lastUpdated = lastUpdated
                )
                trySend(config)
            } catch (e: Exception) {
                android.util.Log.e("FirebaseDbManager", "Error parsing spin wheel config: ${e.message}", e)
                trySend(com.playwin.app.data.model.FirebaseSpinWheelConfig(segments = emptyList()))
            }
        }

        val configListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                latestConfigSnapshot = snapshot
                sendUpdatedConfig()
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("FirebaseDbManager", "observeSpinWheelConfig cancelled. Code: ${error.code}.", error.toException())
            }
        }

        val segmentsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    latestSegmentsSnapshot = snapshot
                    sendUpdatedConfig()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("FirebaseDbManager", "observeSpinWheelConfig segments cancelled. Code: ${error.code}.", error.toException())
            }
        }

        configRef.addValueEventListener(configListener)
        segmentsRef.addValueEventListener(segmentsListener)

        awaitClose {
            configRef.removeEventListener(configListener)
            segmentsRef.removeEventListener(segmentsListener)
        }
    }

    private fun parseSegmentsFromSnapshot(snapshot: DataSnapshot): List<com.playwin.app.data.model.FirebaseSpinReward> {
        val list = mutableListOf<com.playwin.app.data.model.FirebaseSpinReward>()
        for (child in snapshot.children) {
            try {
                val id = child.child("id").getValue(String::class.java) ?: child.key ?: ""
                
                val title = child.child("title").getValue(String::class.java)
                    ?: child.child("name").getValue(String::class.java)
                    ?: ""
                
                val type = child.child("rewardType").getValue(String::class.java)
                    ?: child.child("type").getValue(String::class.java)
                    ?: "Coins"
                
                val rawValue = child.child("value").value ?: child.child("reward").value
                val value = when (rawValue) {
                    is String -> rawValue
                    is Number -> rawValue.toLong().toString()
                    null -> ""
                    else -> rawValue.toString()
                }
                
                val order = when (val orderVal = child.child("order").value ?: child.child("displayOrder").value) {
                    is Number -> orderVal.toInt()
                    is String -> orderVal.toIntOrNull() ?: 0
                    else -> 0
                }
                
                val probability = when (val weightVal = child.child("probability").value ?: child.child("probabilityWeight").value ?: child.child("weight").value) {
                    is Number -> weightVal.toInt()
                    is String -> weightVal.toIntOrNull() ?: 1
                    else -> 1
                }
                
                val statusVal = child.child("status").getValue(String::class.java)
                val activeVal = child.child("active").value ?: child.child("isActive").value ?: child.child("enabled").value
                val active = if (statusVal != null) {
                    statusVal.trim().equals("Active", ignoreCase = true)
                } else {
                    when (activeVal) {
                        is Boolean -> activeVal
                        is String -> activeVal.equals("true", ignoreCase = true) || activeVal.equals("active", ignoreCase = true)
                        is Number -> activeVal.toInt() != 0
                        else -> true
                    }
                }

                val icon = child.child("icon").getValue(String::class.java) ?: ""
                val color = child.child("color").getValue(String::class.java) ?: ""
                val description = child.child("description").getValue(String::class.java) ?: ""
                
                list.add(
                    com.playwin.app.data.model.FirebaseSpinReward(
                        id = id,
                        name = title,
                        title = title,
                        type = type,
                        rewardType = type,
                        value = value,
                        reward = value,
                        displayOrder = order,
                        order = order,
                        probabilityWeight = probability,
                        probability = probability,
                        active = active,
                        enabled = active,
                        icon = icon,
                        color = color,
                        description = description
                    )
                )
            } catch (e: Exception) {
                android.util.Log.e("FirebaseDbManager", "Error parsing segment child: ${e.message}")
            }
        }
        return list
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
                com.playwin.app.data.model.verifyAndLogBooleans(snapshot)
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
                com.playwin.app.data.model.verifyAndLogBooleans(snapshot)
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
                    try {
                        val ref = child.getValue(FirebaseReferral::class.java)
                        if (ref != null && ref.referredUserId == userId) {
                            child.ref.removeValue()
                        }
                    } catch (e: Exception) {
                        // Safe skip malformed entries/strings
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
        val id = if (coupon.couponId.isEmpty()) couponsRef.push().key ?: "" else coupon.couponId
        val finalCoupon = coupon.copy(couponId = id)
        couponsRef.child(finalCoupon.couponId).setValue(finalCoupon)
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
                com.playwin.app.data.model.verifyAndLogBooleans(snapshot)
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

    fun observeCompletedQuizzes(userId: String): kotlinx.coroutines.flow.Flow<Map<String, com.playwin.app.data.model.FirebaseCompletedQuiz>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyMap())
            close()
            return@callbackFlow
        }
        val ref = database.getReference("users/$userId/completedQuizzes")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val completed = mutableMapOf<String, com.playwin.app.data.model.FirebaseCompletedQuiz>()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val valObj = try {
                        child.getValue(com.playwin.app.data.model.FirebaseCompletedQuiz::class.java)
                    } catch (e: Exception) {
                        null
                    }
                    if (valObj != null) {
                        completed[key] = valObj
                    }
                }
                trySend(completed)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun saveCompletedQuiz(userId: String, quizId: String, completedQuiz: com.playwin.app.data.model.FirebaseCompletedQuiz) {
        if (userId.isEmpty() || quizId.isEmpty()) return
        database.getReference("users/$userId/completedQuizzes/$quizId").setValue(completedQuiz)
    }

    fun observeWeeklyQuizProgress(userId: String): kotlinx.coroutines.flow.Flow<Map<String, com.playwin.app.data.model.FirebaseWeeklyQuizProgress>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyMap())
            close()
            return@callbackFlow
        }
        val ref = database.getReference("users/$userId/weeklyQuizProgress")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val progressMap = mutableMapOf<String, com.playwin.app.data.model.FirebaseWeeklyQuizProgress>()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val valObj = try {
                        child.getValue(com.playwin.app.data.model.FirebaseWeeklyQuizProgress::class.java)
                    } catch (e: Exception) {
                        null
                    }
                    if (valObj != null) {
                        progressMap[key] = valObj
                    }
                }
                trySend(progressMap)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun saveWeeklyQuizProgress(userId: String, dayOfWeek: String, progress: com.playwin.app.data.model.FirebaseWeeklyQuizProgress) {
        if (userId.isEmpty() || dayOfWeek.isEmpty()) return
        database.getReference("users/$userId/weeklyQuizProgress/$dayOfWeek").setValue(progress)
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
        var failureReason: String? = null

        android.util.Log.d("FirebaseDbManager", "BEFORE TRANSACTION LOG:")
        android.util.Log.d("FirebaseDbManager", "Coupon ID = $couponId")
        android.util.Log.d("FirebaseDbManager", "Coupon Name = ${redemption.couponName}")
        android.util.Log.d("FirebaseDbManager", "Database Path = /coupons/$couponId")

        if (couponId.trim().isEmpty()) {
            onError("INVALID_COUPON_ID")
            return
        }

        val couponRef = database.getReference("coupons").child(couponId)
        couponRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                // 0. Handle initial local cache null value safely.
                if (currentData.value == null) {
                    android.util.Log.d("FirebaseDbManager", "Initial local transaction run is null. Proceeding to force server-sync retry...")
                    return com.google.firebase.database.Transaction.success(currentData)
                }

                // Since we run on /coupons/{couponId}, currentData IS the coupon node itself.
                val remainingStockNode = currentData.child("remainingStock")
                val stockNode = currentData.child("stock")
                val remainingStockRaw = remainingStockNode.value
                val stockRaw = stockNode.value

                // Logging details
                android.util.Log.d("FirebaseDbManager", "COUPON TRANSACTION ATTEMPT DETAILS:")
                android.util.Log.d("FirebaseDbManager", "couponId: $couponId")
                android.util.Log.d("FirebaseDbManager", "coupon path: /coupons/$couponId")
                android.util.Log.d("FirebaseDbManager", "remainingStock: $remainingStockRaw")
                android.util.Log.d("FirebaseDbManager", "stock: $stockRaw")

                // Check if coupon node has fields to ensure it actually exists (since it's a map)
                if (currentData.value !is Map<*, *>) {
                    failureReason = "COUPON_NOT_FOUND"
                    android.util.Log.e("FirebaseDbManager", "TRANSACTION ABORTED: COUPON_NOT_FOUND")
                    return com.google.firebase.database.Transaction.abort()
                }

                // Check remainingStock == null and stock == null
                if (remainingStockRaw == null && stockRaw == null) {
                    failureReason = "STOCK_FIELD_MISSING"
                    android.util.Log.e("FirebaseDbManager", "TRANSACTION ABORTED: STOCK_FIELD_MISSING")
                    return com.google.firebase.database.Transaction.abort()
                }

                val couponStockVal = when {
                    remainingStockRaw is Number -> remainingStockRaw.toInt()
                    remainingStockRaw is String -> remainingStockRaw.toIntOrNull() ?: 50
                    stockRaw is Number -> stockRaw.toInt()
                    stockRaw is String -> stockRaw.toIntOrNull() ?: 50
                    else -> 0
                }

                if (couponStockVal <= 0) {
                    failureReason = "OUT_OF_STOCK"
                    android.util.Log.e("FirebaseDbManager", "TRANSACTION ABORTED: OUT_OF_STOCK")
                    return com.google.firebase.database.Transaction.abort()
                }

                // Reduce stock by 1
                if (remainingStockRaw != null) {
                    remainingStockNode.value = couponStockVal - 1
                } else if (stockRaw != null) {
                    stockNode.value = couponStockVal - 1
                } else {
                    remainingStockNode.value = couponStockVal - 1
                }

                // Update redeemedCount
                val redeemedCountNode = currentData.child("redeemedCount")
                val redeemedCountRaw = redeemedCountNode.value
                val redeemedCountVal = when (redeemedCountRaw) {
                    is Number -> redeemedCountRaw.toInt()
                    is String -> redeemedCountRaw.toIntOrNull() ?: 0
                    else -> 0
                }
                redeemedCountNode.value = redeemedCountVal + 1

                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(
                error: com.google.firebase.database.DatabaseError?,
                committed: Boolean,
                currentDataSnapshot: com.google.firebase.database.DataSnapshot?
            ) {
                if (error != null) {
                    onError(error.message)
                    return
                }
                if (!committed) {
                    onError(failureReason ?: "TRANSACTION_ABORTED")
                    return
                }

                if (currentDataSnapshot == null || currentDataSnapshot.value == null) {
                    onError("COUPON_NOT_FOUND")
                    return
                }

                android.util.Log.d("FirebaseDbManager", "Coupon stock updated successfully. Moving to deduct user coins...")
                deductUserCoins(redemption, couponId, onSuccess, onError)
            }
        })
    }

    private fun rollbackCouponTransaction(couponId: String, onRollbackComplete: () -> Unit) {
        val couponRef = database.getReference("coupons").child(couponId)
        couponRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                if (currentData.value == null) {
                    return com.google.firebase.database.Transaction.success(currentData)
                }
                val remainingStockNode = currentData.child("remainingStock")
                val stockNode = currentData.child("stock")
                val remainingStockRaw = remainingStockNode.value
                val stockRaw = stockNode.value
                
                val couponStockVal = when {
                    remainingStockRaw is Number -> remainingStockRaw.toInt()
                    remainingStockRaw is String -> remainingStockRaw.toIntOrNull() ?: 50
                    stockRaw is Number -> stockRaw.toInt()
                    stockRaw is String -> stockRaw.toIntOrNull() ?: 50
                    else -> 0
                }

                if (remainingStockRaw != null) {
                    remainingStockNode.value = couponStockVal + 1
                } else if (stockRaw != null) {
                    stockNode.value = couponStockVal + 1
                } else {
                    remainingStockNode.value = couponStockVal + 1
                }

                val redeemedCountNode = currentData.child("redeemedCount")
                val redeemedCountRaw = redeemedCountNode.value
                val redeemedCountVal = when (redeemedCountRaw) {
                    is Number -> redeemedCountRaw.toInt()
                    is String -> redeemedCountRaw.toIntOrNull() ?: 0
                    else -> 0
                }
                if (redeemedCountVal > 0) {
                    redeemedCountNode.value = redeemedCountVal - 1
                }

                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(
                error: com.google.firebase.database.DatabaseError?,
                committed: Boolean,
                currentData: com.google.firebase.database.DataSnapshot?
            ) {
                android.util.Log.d("FirebaseDbManager", "Rollback coupon stock completed (success=$committed)")
                onRollbackComplete()
            }
        })
    }

    private fun deductUserCoins(
        redemption: com.playwin.app.data.model.FirebaseCouponRedemption,
        couponId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = redemption.userUid
        val requiredCoins = redemption.requiredCoins
        var userCoinsBefore = 0
        var userCoinsAfter = 0

        var userFailureReason: String? = null
        val userRef = database.getReference("users").child(userId)
        userRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                if (currentData.value == null) {
                    return com.google.firebase.database.Transaction.success(currentData)
                }

                val coinsRaw = currentData.child("coins").value
                val coinsVal = when (coinsRaw) {
                    is Number -> coinsRaw.toInt()
                    is String -> coinsRaw.toIntOrNull() ?: 0
                    else -> 0
                }

                if (coinsVal < requiredCoins) {
                    userFailureReason = "INSUFFICIENT_COINS"
                    return com.google.firebase.database.Transaction.abort()
                }

                userCoinsBefore = coinsVal
                userCoinsAfter = coinsVal - requiredCoins
                currentData.child("coins").value = userCoinsAfter

                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(
                error: com.google.firebase.database.DatabaseError?,
                committed: Boolean,
                currentDataSnapshot: com.google.firebase.database.DataSnapshot?
            ) {
                if (error != null || !committed || currentDataSnapshot == null || currentDataSnapshot.value == null) {
                    // If coin deduction failed, roll back the coupon stock
                    rollbackCouponTransaction(couponId) {
                        onError(error?.message ?: userFailureReason ?: "INSUFFICIENT_COINS")
                    }
                    return
                }

                // Coin deduction succeeded! Proceed to create records outside transaction
                createRedemptionRecords(redemption, couponId, userCoinsBefore, userCoinsAfter, onSuccess, onError)
            }
        })
    }

    private fun createRedemptionRecords(
        redemption: com.playwin.app.data.model.FirebaseCouponRedemption,
        couponId: String,
        coinsBefore: Int,
        coinsAfter: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = redemption.userUid
        val requestId = redemption.requestId
        val couponName = redemption.couponName
        val requiredCoins = redemption.requiredCoins
        val serverTime = System.currentTimeMillis()

        // 2. Create the redemption record using updateChildren() outside the transaction.
        // Under /couponRedemptions/{requestId}
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

        // And legacy redemption record under /redemptions/{userId}/{requestId}
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

        val updates = mapOf(
            "couponRedemptions/$requestId" to redemptionMap,
            "redemptions/$userId/$requestId" to legacyRedemptionMap
        )

        database.reference.updateChildren(updates)
            .addOnSuccessListener {
                // 3. Create transaction history outside the transaction.
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
                    "coinsBefore" to coinsBefore,
                    "coinsAfter" to coinsAfter
                )
                database.reference.child("transactions").child(userId).child(txId).setValue(txMap)
                    .addOnSuccessListener {
                        // 4. Update walletSummary outside the transaction.
                        val walletSummaryMap = mapOf(
                            "totalCoins" to coinsAfter,
                            "lastUpdated" to serverTime
                        )
                        database.reference.child("walletSummary").child(userId).setValue(walletSummaryMap)
                            .addOnSuccessListener {
                                // 5. Create admin logs outside the transaction.
                                val logId = "log_" + serverTime + "_" + (1000..9999).random()
                                val adminLogMap = mapOf(
                                    "id" to logId,
                                    "action" to "COUPON_REDEMPTION_SUBMITTED",
                                    "message" to "User $userId submitted redemption request for $couponName (Required: $requiredCoins coins)",
                                    "timestamp" to serverTime,
                                    "userId" to userId,
                                    "couponId" to couponId
                                )
                                database.reference.child("adminLogs").child(logId).setValue(adminLogMap)
                                    .addOnSuccessListener {
                                        onSuccess()
                                    }
                                    .addOnFailureListener { e ->
                                        // Even if admin log fails, redemption succeeded, so we can call onSuccess or onError
                                        android.util.Log.e("FirebaseDbManager", "Failed to write admin log", e)
                                        onSuccess()
                                    }
                            }
                            .addOnFailureListener { e ->
                                android.util.Log.e("FirebaseDbManager", "Failed to update wallet summary", e)
                                // We can still proceed as the core transaction and records are updated
                                onSuccess()
                            }
                    }
                    .addOnFailureListener { e ->
                        onError("Failed to create transaction history: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                // Refund and rollback if creating redemption records fails
                refundCoinsAndRollbackCoupon(userId, couponId, requiredCoins) {
                    onError("Failed to create redemption records: ${e.message}")
                }
            }
    }

    private fun refundCoinsAndRollbackCoupon(
        userId: String,
        couponId: String,
        requiredCoins: Int,
        onRollbackComplete: () -> Unit
    ) {
        val userRef = database.getReference("users").child(userId)
        userRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                if (currentData.value == null) {
                    return com.google.firebase.database.Transaction.success(currentData)
                }
                val coinsRaw = currentData.child("coins").value
                val coinsVal = when (coinsRaw) {
                    is Number -> coinsRaw.toInt()
                    is String -> coinsRaw.toIntOrNull() ?: 0
                    else -> 0
                }
                currentData.child("coins").value = coinsVal + requiredCoins
                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(
                error: com.google.firebase.database.DatabaseError?,
                committed: Boolean,
                currentDataSnapshot: com.google.firebase.database.DataSnapshot?
            ) {
                rollbackCouponTransaction(couponId) {
                    onRollbackComplete()
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

    fun observeQuizzes(): kotlinx.coroutines.flow.Flow<List<com.playwin.app.data.model.FirebaseQuiz>> = callbackFlow {
        val ref = database.getReference("quizzes")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<com.playwin.app.data.model.FirebaseQuiz>()
                if (!snapshot.exists()) {
                    trySend(emptyList())
                    return
                }
                for (child in snapshot.children) {
                    try {
                        val idVal = child.child("id").value ?: child.child("quizId").value ?: child.key ?: ""
                        val id = idVal.toString()
                        if (id.isEmpty()) continue

                        val title = (child.child("title").value ?: child.child("quizTitle").value ?: child.child("name").value ?: "").toString()
                        val categoryId = (child.child("categoryId").value ?: child.child("category").value ?: "").toString()
                        val categoryName = (child.child("categoryName").value ?: child.child("category").value ?: child.child("categoryId").value ?: "").toString()
                        val category = (child.child("category").value ?: child.child("categoryId").value ?: "").toString()
                        val description = (child.child("description").value ?: child.child("desc").value ?: "").toString()
                        val difficulty = (child.child("difficulty").value ?: child.child("level").value ?: "Medium").toString()
                        
                        val rewardPerQuestion = (child.child("rewardPerQuestion").value ?: child.child("rewardCoins").value ?: child.child("reward").value ?: child.child("coins").value ?: 0).toString().toIntOrNull() ?: 0
                        val rewardCoins = rewardPerQuestion
                        val passBonus = (child.child("passBonus").value ?: child.child("completionBonus").value ?: child.child("bonus").value ?: child.child("perfectBonus").value ?: 0).toString().toIntOrNull() ?: 0
                        val completionBonus = passBonus
                        val passingPercentage = (child.child("passingPercentage").value ?: 0).toString().toIntOrNull() ?: 0
                        val timerSeconds = (child.child("timerSeconds").value ?: child.child("timer").value ?: child.child("timeLimit").value ?: child.child("duration").value ?: 30).toString().toIntOrNull() ?: 30
                        val icon = (child.child("icon").value ?: child.child("thumbnail").value ?: child.child("imageUrl").value ?: "").toString()
                        
                        val pubVal = child.child("published").value ?: child.child("isPublished").value
                        val published = if (pubVal is Boolean) pubVal else pubVal?.toString()?.toBoolean() ?: false
                        
                        val actVal = child.child("active").value ?: child.child("isActive").value
                        val active = if (actVal is Boolean) actVal else actVal?.toString()?.toBoolean() ?: false

                        val status = (child.child("status").value ?: "").toString()
                        
                        // 1. Filter: status == "Published" OR published == true. Ignore Draft, Disabled and Inactive quizzes.
                        val statusLower = status.lowercase().trim()
                        if (statusLower == "draft" || statusLower == "disabled" || statusLower == "inactive" || statusLower == "drafts") {
                            continue
                        }
                        val isPublished = status.equals("Published", ignoreCase = true) || published
                        if (!isPublished) {
                            continue
                        }

                        val revVal = child.child("allowReview").value ?: child.child("reviewEnabled").value ?: true
                        val allowReview = if (revVal is Boolean) revVal else revVal.toString().toBoolean()

                        val questionsList = mutableListOf<com.playwin.app.data.model.Quiz>()
                        val questionsSnap = child.child("questions")
                        if (questionsSnap.exists()) {
                            for (qChild in questionsSnap.children) {
                                try {
                                    val qId = (qChild.child("id").value ?: qChild.child("questionId").value ?: qChild.key ?: "").toString()
                                    val qText = (qChild.child("question").value ?: qChild.child("title").value ?: qChild.child("text").value ?: "").toString()
                                    
                                    val options = mutableListOf<String>()
                                    val optSnap = qChild.child("options")
                                    if (optSnap.exists()) {
                                        for (optChild in optSnap.children) {
                                            optChild.value?.toString()?.let { options.add(it) }
                                        }
                                    } else {
                                        val oA = qChild.child("optionA").value ?: qChild.child("option_A").value
                                        val oB = qChild.child("optionB").value ?: qChild.child("option_B").value
                                        val oC = qChild.child("optionC").value ?: qChild.child("option_C").value
                                        val oD = qChild.child("optionD").value ?: qChild.child("option_D").value
                                        if (oA != null || oB != null || oC != null || oD != null) {
                                            if (oA != null) options.add(oA.toString())
                                            if (oB != null) options.add(oB.toString())
                                            if (oC != null) options.add(oC.toString())
                                            if (oD != null) options.add(oD.toString())
                                        } else {
                                            for (i in 1..4) {
                                                val oVal = qChild.child("option$i").value ?: qChild.child("option_$i").value
                                                if (oVal != null) {
                                                    options.add(oVal.toString())
                                                }
                                            }
                                        }
                                    }
                                    
                                    val correctVal = qChild.child("correctAnswer").value 
                                        ?: qChild.child("correctAnswerIdx").value 
                                        ?: qChild.child("correctAnswerIndex").value 
                                        ?: qChild.child("answerIdx").value 
                                        ?: qChild.child("answer").value 
                                        ?: "0"
                                    val correctString = correctVal.toString().trim()
                                    val correctIdx = when (correctString.uppercase()) {
                                        "A" -> 0
                                        "B" -> 1
                                        "C" -> 2
                                        "D" -> 3
                                        "1" -> 0
                                        "2" -> 1
                                        "3" -> 2
                                        "4" -> 3
                                        else -> correctString.toIntOrNull() ?: 0
                                    }
                                    
                                    val explanation = (qChild.child("explanation").value ?: "").toString()
                                    
                                    if (qText.isNotEmpty()) {
                                        questionsList.add(com.playwin.app.data.model.Quiz(qId, qText, options, correctIdx, explanation))
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("FirebaseDbManager", "Error parsing dynamic question", e)
                                }
                            }
                        }

                        // 5. Hide the quiz if questions count is 0
                        if (questionsList.isEmpty()) {
                            continue
                        }

                        val quizItem = com.playwin.app.data.model.FirebaseQuiz(
                            id = id,
                            title = title,
                            categoryId = categoryId,
                            categoryName = categoryName,
                            category = category,
                            description = description,
                            difficulty = difficulty,
                            rewardPerQuestion = rewardPerQuestion,
                            rewardCoins = rewardCoins,
                            passBonus = passBonus,
                            completionBonus = completionBonus,
                            passingPercentage = passingPercentage,
                            timerSeconds = timerSeconds,
                            icon = icon,
                            published = published,
                            active = active,
                            status = status,
                            allowReview = allowReview,
                            questions = questionsList
                        )
                        list.add(quizItem)
                    } catch (e: Exception) {
                        android.util.Log.e("FirebaseDbManager", "Error parsing dynamic quiz", e)
                    }
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeScratchCardSettings(): Flow<com.playwin.app.data.model.FirebaseScratchCardSettings> = callbackFlow {
        val ref = database.getReference("admin/gameSettings/scratchCard")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val defaultSettings = com.playwin.app.data.model.FirebaseScratchCardSettings()
                    trySend(defaultSettings)
                    return
                }
                try {
                    val enabledVal = snapshot.child("enabled").value ?: snapshot.child("active").value ?: true
                    val enabled = when (enabledVal) {
                        is Boolean -> enabledVal
                        is String -> enabledVal.toBoolean()
                        is Number -> enabledVal.toInt() != 0
                        else -> true
                    }
                    val dailyScratchLimitVal = snapshot.child("dailyScratchLimit").value ?: snapshot.child("dailyLimit").value
                    val dailyScratchLimit = when (dailyScratchLimitVal) {
                        is Number -> dailyScratchLimitVal.toInt()
                        is String -> dailyScratchLimitVal.toIntOrNull() ?: 5
                        else -> 5
                    }
                    val dailyFreeScratchVal = snapshot.child("dailyFreeScratch").value ?: snapshot.child("freeScratches").value
                    val dailyFreeScratch = when (dailyFreeScratchVal) {
                        is Number -> dailyFreeScratchVal.toInt()
                        is String -> dailyFreeScratchVal.toIntOrNull() ?: 1
                        else -> 1
                    }
                    val rewardedScratchEnabledVal = snapshot.child("rewardedScratchEnabled").value
                    val rewardedScratchEnabled = when (rewardedScratchEnabledVal) {
                        is Boolean -> rewardedScratchEnabledVal
                        is String -> rewardedScratchEnabledVal.toBoolean()
                        is Number -> rewardedScratchEnabledVal.toInt() != 0
                        else -> true
                    }
                    val requireAdForEveryExtraScratchVal = snapshot.child("requireAdForEveryExtraScratch").value ?: snapshot.child("rewardAdRequired").value
                    val requireAdForEveryExtraScratch = when (requireAdForEveryExtraScratchVal) {
                        is Boolean -> requireAdForEveryExtraScratchVal
                        is String -> requireAdForEveryExtraScratchVal.toBoolean()
                        is Number -> requireAdForEveryExtraScratchVal.toInt() != 0
                        else -> true
                    }
                    val maxRewardedScratchPerDayVal = snapshot.child("maxRewardedScratchPerDay").value ?: snapshot.child("maxRewardedSpins").value
                    val maxRewardedScratchPerDay = when (maxRewardedScratchPerDayVal) {
                        is Number -> maxRewardedScratchPerDayVal.toInt()
                        is String -> maxRewardedScratchPerDayVal.toIntOrNull() ?: 5
                        else -> 5
                    }
                    val rewardCooldownMinutesVal = snapshot.child("rewardCooldownMinutes").value ?: snapshot.child("cooldownMinutes").value
                    val rewardCooldownMinutes = when (rewardCooldownMinutesVal) {
                        is Number -> rewardCooldownMinutesVal.toInt()
                        is String -> rewardCooldownMinutesVal.toIntOrNull() ?: 0
                        else -> 0
                    }
                    val minimumUserLevelVal = snapshot.child("minimumUserLevel").value ?: snapshot.child("minimumLevel").value
                    val minimumUserLevel = when (minimumUserLevelVal) {
                        is Number -> minimumUserLevelVal.toInt()
                        is String -> minimumUserLevelVal.toIntOrNull() ?: 1
                        else -> 1
                    }
                    val settings = com.playwin.app.data.model.FirebaseScratchCardSettings(
                        enabled = enabled,
                        dailyScratchLimit = dailyScratchLimit,
                        dailyFreeScratch = dailyFreeScratch,
                        rewardedScratchEnabled = rewardedScratchEnabled,
                        requireAdForEveryExtraScratch = requireAdForEveryExtraScratch,
                        maxRewardedScratchPerDay = maxRewardedScratchPerDay,
                        rewardCooldownMinutes = rewardCooldownMinutes,
                        minimumUserLevel = minimumUserLevel
                    )
                    trySend(settings)
                } catch (e: Exception) {
                    trySend(com.playwin.app.data.model.FirebaseScratchCardSettings())
                }
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(com.playwin.app.data.model.FirebaseScratchCardSettings())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeUserScratchCardState(userId: String): Flow<com.playwin.app.data.model.FirebaseUserScratchCardState> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(com.playwin.app.data.model.FirebaseUserScratchCardState())
            close()
            return@callbackFlow
        }
        val ref = database.getReference("users").child(userId).child("scratchCard")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(com.playwin.app.data.model.FirebaseUserScratchCardState())
                    return
                }
                try {
                    val state = snapshot.getValue(com.playwin.app.data.model.FirebaseUserScratchCardState::class.java)
                    if (state != null) {
                        trySend(state)
                    } else {
                        trySend(com.playwin.app.data.model.FirebaseUserScratchCardState())
                    }
                } catch (e: Exception) {
                    trySend(com.playwin.app.data.model.FirebaseUserScratchCardState())
                }
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(com.playwin.app.data.model.FirebaseUserScratchCardState())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeScratchCardRewards(): Flow<List<com.playwin.app.data.model.FirebaseScratchCardReward>> = callbackFlow {
        val ref = database.getReference("scratchCardRewards")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                    val initialRewards = listOf(
                        com.playwin.app.data.model.FirebaseScratchCardReward(
                            id = "reward_1", name = "+10 Coins", type = "Coins", value = "10",
                            probabilityWeight = 40, status = "Active", displayOrder = 1, active = true, icon = "🪙", color = "#FFD700"
                        ),
                        com.playwin.app.data.model.FirebaseScratchCardReward(
                            id = "reward_2", name = "+50 Coins", type = "Coins", value = "50",
                            probabilityWeight = 20, status = "Active", displayOrder = 2, active = true, icon = "💎", color = "#00E5FF"
                        ),
                        com.playwin.app.data.model.FirebaseScratchCardReward(
                            id = "reward_3", name = "+100 Coins", type = "Coins", value = "100",
                            probabilityWeight = 10, status = "Active", displayOrder = 3, active = true, icon = "🎁", color = "#E040FB"
                        ),
                        com.playwin.app.data.model.FirebaseScratchCardReward(
                            id = "reward_4", name = "Retry Scratch", type = "Retry Scratch", value = "0",
                            probabilityWeight = 15, status = "Active", displayOrder = 4, active = true, icon = "🔄", color = "#00E676"
                        ),
                        com.playwin.app.data.model.FirebaseScratchCardReward(
                            id = "reward_5", name = "Amazon Coupon", type = "Coupon", value = "AMZ-PW-100",
                            probabilityWeight = 5, status = "Active", displayOrder = 5, active = true, icon = "🛍️", color = "#FF9100"
                        ),
                        com.playwin.app.data.model.FirebaseScratchCardReward(
                            id = "reward_6", name = "Better Luck Next Time", type = "Better Luck Next Time", value = "0",
                            probabilityWeight = 10, status = "Active", displayOrder = 6, active = true, icon = "😢", color = "#90A4AE"
                        )
                    )
                    for (reward in initialRewards) {
                        ref.child(reward.id).setValue(reward)
                    }
                    trySend(initialRewards)
                    return
                }

                val list = mutableListOf<com.playwin.app.data.model.FirebaseScratchCardReward>()
                android.util.Log.d("PlayWinScratchDebug", "--- [STEP 1] LOADING REWARDS FROM FIREBASE ---")
                for (child in snapshot.children) {
                    try {
                        val id = child.child("id").value?.toString() ?: child.key ?: ""
                        val name = child.child("name").value?.toString() ?: ""
                        val type = child.child("type").value?.toString() ?: "Coins"
                        val value = (child.child("value").value ?: "0").toString()

                        val weightVal = child.child("probabilityWeight").value
                        val probabilityWeight = when (weightVal) {
                            is Number -> weightVal.toInt()
                            is String -> weightVal.toIntOrNull() ?: 10
                            else -> 10
                        }

                        val orderVal = child.child("displayOrder").value
                        val displayOrder = when (orderVal) {
                            is Number -> orderVal.toInt()
                            is String -> orderVal.toIntOrNull() ?: 0
                            else -> 0
                        }

                        val status = child.child("status").value?.toString() ?: "Active"
                        val isStatusActive = !status.equals("inactive", ignoreCase = true) && 
                                             !status.equals("disabled", ignoreCase = true) &&
                                             !status.equals("false", ignoreCase = true)

                        val activeVal = child.child("active").value
                        val active = when (activeVal) {
                            is Boolean -> activeVal && isStatusActive
                            is String -> activeVal.toBoolean() && isStatusActive
                            else -> isStatusActive
                        }

                        val icon = child.child("icon").value?.toString() ?: "🎁"
                        val color = child.child("color").value?.toString() ?: "#7C4DFF"

                        val createdAtVal = child.child("createdAt").value
                        val createdAt = when (createdAtVal) {
                            is Number -> createdAtVal.toLong()
                            is String -> createdAtVal.toLongOrNull() ?: System.currentTimeMillis()
                            else -> System.currentTimeMillis()
                        }

                        val updatedAtVal = child.child("updatedAt").value
                        val updatedAt = when (updatedAtVal) {
                            is Number -> updatedAtVal.toLong()
                            is String -> updatedAtVal.toLongOrNull() ?: System.currentTimeMillis()
                            else -> System.currentTimeMillis()
                        }

                        val reward = com.playwin.app.data.model.FirebaseScratchCardReward(
                            id = id,
                            name = name,
                            type = type,
                            value = value,
                            probabilityWeight = probabilityWeight,
                            status = status,
                            displayOrder = displayOrder,
                            active = active,
                            icon = icon,
                            color = color,
                            createdAt = createdAt,
                            updatedAt = updatedAt
                        )
                        android.util.Log.d("PlayWinScratchDebug", "Deserialized reward: $reward")
                        list.add(reward)
                    } catch (e: Exception) {
                        android.util.Log.e("PlayWinScratchDebug", "Error parsing individual reward child: ${child.key}", e)
                    }
                }

                // Print all rewards loaded
                android.util.Log.d("PlayWinScratchDebug", "[STEP 2] ALL REWARDS LOADED (${list.size}):")
                list.forEach { r ->
                    android.util.Log.d("PlayWinScratchDebug", "  - ID: ${r.id}, Name: ${r.name}, Type: ${r.type}, Value: ${r.value}, Active: ${r.active}, Weight: ${r.probabilityWeight}")
                }

                // Print only active rewards
                val activeList = list.filter { it.active }
                android.util.Log.d("PlayWinScratchDebug", "[STEP 3] ACTIVE REWARDS ONLY (${activeList.size}):")
                activeList.forEach { r ->
                    android.util.Log.d("PlayWinScratchDebug", "  - ID: ${r.id}, Name: ${r.name}, Type: ${r.type}, Value: ${r.value}, Weight: ${r.probabilityWeight}")
                }

                list.sortBy { it.displayOrder }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun performScratchCardDbTransaction(
        userId: String,
        reward: com.playwin.app.data.model.FirebaseScratchCardReward,
        transactionId: String,
        isAdScratch: Boolean,
        onComplete: (Boolean, String?, Int, Int) -> Unit
    ) {
        val typeSafe = reward.type?.trim() ?: "Coins"
        val isCoins = typeSafe.equals("Coins", ignoreCase = true) || 
                      (!typeSafe.equals("Coupon", ignoreCase = true) && 
                       !typeSafe.contains("retry", ignoreCase = true) && 
                       !typeSafe.contains("luck", ignoreCase = true) && 
                       ((reward.value ?: "0").toIntOrNull() ?: 0) > 0)

        val coinIncrement = if (isCoins) {
            val parsed = (reward.value ?: "0").toIntOrNull()
            if (parsed == null) {
                android.util.Log.e("PlayWinScratchDebug", "PARSING FAILURE: Field 'value' with content '${reward.value}' could not be parsed as an Integer for Coins reward ID '${reward.id}'.")
            }
            parsed ?: 0
        } else {
            0
        }
        
        // 12. If reward.type == Coins and reward.value > 0:
        if (isCoins && coinIncrement <= 0) {
            val errorMsg = "Coins reward value is $coinIncrement (must be > 0 or valid integer)"
            android.util.Log.e("PlayWinScratchDebug", "Transaction failure: $errorMsg")
            onComplete(false, errorMsg, 0, 0)
            return
        }

        android.util.Log.d("PlayWinScratchDebug", "Starting secure server Scratch Card Db Transaction for uid: $userId, txId: $transactionId, isAdScratch: $isAdScratch")

        val userRef = database.getReference("users").child(userId)
        userRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(mutableData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                if (mutableData.child("processedScratchTx").child(transactionId).value != null) {
                    return com.google.firebase.database.Transaction.abort()
                }

                // Increment Coins in wallet
                val walletRef = mutableData.child("wallet")
                val currentCoins = walletRef.child("coins").getValue(Int::class.java) ?: 0
                walletRef.child("coins").value = currentCoins + coinIncrement
                walletRef.child("balance").value = currentCoins + coinIncrement
                walletRef.child("currentCoins").value = currentCoins + coinIncrement

                val totalRewardsVal = walletRef.child("totalScratchRewards").getValue(Int::class.java) ?: 0
                walletRef.child("totalScratchRewards").value = totalRewardsVal + coinIncrement

                val totalCoinsEarned = walletRef.child("totalCoinsEarned").getValue(Int::class.java) ?: 0
                walletRef.child("totalCoinsEarned").value = totalCoinsEarned + coinIncrement

                // Mark transaction as processed
                mutableData.child("processedScratchTx").child(transactionId).value = true

                // Update users/{uid}/scratchCard node with server timestamp and increment counts
                val scratchCardNode = mutableData.child("scratchCard")
                val currentServerTime = System.currentTimeMillis()

                val nextReset = scratchCardNode.child("nextResetTimestamp").getValue(Long::class.java) ?: 0L
                var freeScratchUsed = scratchCardNode.child("freeScratchUsed").getValue(Int::class.java) ?: 0
                var rewardedScratchUsed = scratchCardNode.child("rewardedScratchUsed").getValue(Int::class.java) ?: 0
                var scratchesToday = scratchCardNode.child("scratchesToday").getValue(Int::class.java) ?: 0

                if (currentServerTime >= nextReset) {
                    freeScratchUsed = 0
                    rewardedScratchUsed = 0
                    scratchesToday = 0
                    
                    val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                    calendar.timeInMillis = currentServerTime
                    calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                    calendar.set(java.util.Calendar.MINUTE, 0)
                    calendar.set(java.util.Calendar.SECOND, 0)
                    calendar.set(java.util.Calendar.MILLISECOND, 0)
                    scratchCardNode.child("nextResetTimestamp").value = calendar.timeInMillis
                }

                if (isAdScratch) {
                    rewardedScratchUsed += 1
                    scratchCardNode.child("lastRewardedAdTimestamp").value = com.google.firebase.database.ServerValue.TIMESTAMP
                } else {
                    freeScratchUsed += 1
                }
                scratchesToday += 1

                scratchCardNode.child("freeScratchUsed").value = freeScratchUsed
                scratchCardNode.child("rewardedScratchUsed").value = rewardedScratchUsed
                scratchCardNode.child("scratchesToday").value = scratchesToday
                scratchCardNode.child("lastScratchTimestamp").value = com.google.firebase.database.ServerValue.TIMESTAMP

                // Legacy scratch fields update for compatibility
                val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                mutableData.child("lastScratchDate").value = todayStr
                mutableData.child("scratchesToday").value = scratchesToday
                mutableData.child("lastScratchResetTime").value = currentServerTime

                return com.google.firebase.database.Transaction.success(mutableData)
            }

            override fun onComplete(
                error: com.google.firebase.database.DatabaseError?,
                committed: Boolean,
                currentData: com.google.firebase.database.DataSnapshot?
            ) {
                if (committed && currentData != null) {
                    val walletSnapshot = currentData.child("wallet")
                    val coinsAfter = walletSnapshot.child("coins").getValue(Int::class.java) ?: 0
                    val coinsBefore = coinsAfter - coinIncrement

                    val timestamp = System.currentTimeMillis()
                    val tx = FirebaseTransaction(
                        id = transactionId,
                        userId = userId,
                        type = "scratch_reward",
                        title = "Scratch Card: ${reward.name}",
                        coins = coinIncrement,
                        status = "Completed",
                        timestamp = timestamp,
                        amount = coinIncrement,
                        source = "Scratch Card: ${reward.name}",
                        coinsBefore = coinsBefore,
                        coinsAfter = coinsAfter
                    )
                    database.getReference("users").child(userId).child("transactions").child(transactionId).setValue(tx)
                    database.getReference("users").child(userId).child("wallet").child("history").child(transactionId).setValue(tx)
                    
                    val historyId = database.getReference("users").child(userId).child("scratchHistory").push().key ?: "sc_$timestamp"
                    val deviceTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                    val history = com.playwin.app.data.model.FirebaseScratchHistory(
                        id = historyId,
                        timestamp = timestamp,
                        rewardId = reward.id,
                        rewardName = reward.name,
                        rewardType = reward.type,
                        rewardValue = reward.value,
                        walletBefore = coinsBefore,
                        walletAfter = coinsAfter,
                        status = "Completed",
                        deviceTime = deviceTime,
                        serverTime = timestamp,
                        transactionId = transactionId
                    )
                    database.getReference("users").child(userId).child("scratchHistory").child(historyId).setValue(history)

                    onComplete(true, null, coinsBefore, coinsAfter)
                } else {
                    onComplete(false, error?.message ?: "Transaction aborted or check failed.", 0, 0)
                }
            }
        })
    }
}


