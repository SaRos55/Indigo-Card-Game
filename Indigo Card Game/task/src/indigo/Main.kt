package indigo

import java.lang.NumberFormatException

enum class Rank(val string: String) {
    KING("K"),
    QUEEN("Q"),
    JACK("J"),
    TEN("10"),
    NINE("9"),
    EIGHT("8"),
    SEVEN("7"),
    SIX("6"),
    FIVE("5"),
    FOUR("4"),
    THREE("3"),
    TWO("2"),
    ACE("A")
}

enum class Suit(val char: Char) {
    CLUBS('♣'),
    DIAMONDS('♦'),
    HEARTS('♥'),
    SPADES('♠')
}

data class Card(val rank: Rank, val suit: Suit) {
    override fun toString() = rank.string + suit.char
}

class Deck {
    private val deck = mutableListOf<Card>()

    fun reset() {
        deck.clear()
        for (s in Suit.values()) {
            for (r in Rank.values()) {
                deck.add(Card(r, s))
            }
        }
    }

    fun get(numberOfCards: Int): List<Card> {
        try {
            if (numberOfCards !in 1..52) throw NumberFormatException()
        } catch (e: NumberFormatException) {
            println("Invalid number of cards.")
            return listOf()
        }
        if (deck.size < numberOfCards) {
            println("The remaining cards are insufficient to meet the request.")
            return deck
        }
        val result = deck.take(numberOfCards)
        deck.removeAll(result)
        return result
    }

    fun shuffle() {
        val shuffledDeck = deck.shuffled()
        deck.clear()
        deck.addAll(shuffledDeck)
    }

    fun isNotEmpty() = deck.isNotEmpty()
}

fun main() {
    println("Indigo Card Game")
    val deck = Deck()
    deck.reset()
    deck.shuffle()
    var playFirst = true
    var winFirst = true
    val table = mutableListOf<Card>()
    val hands = mutableListOf<Card>()
    val winHands = mutableListOf<Card>()
    val computer = mutableListOf<Card>()
    val winComputer = mutableListOf<Card>()
    do {
        println("Play first?")
        val input = readln().lowercase()
        playFirst = when (input) {
            "yes" -> true
            "no" -> false
            else -> continue
        }
    } while (!("yes|no".toRegex().matches(input)))
    table.addAll(deck.get(4))
    hands.addAll(deck.get(6))
    computer.addAll(deck.get(6))
    print("Initial cards on the table: ")
    table.forEach { print("$it ") }
    repeat(2) { println() }
    println("${table.size} cards on the table, and the top card is ${table.last()}")
    var input = ""
    while (hands.isNotEmpty() || computer.isNotEmpty() || deck.isNotEmpty()) {
        if (playFirst) {
            if (hands.isEmpty()) hands.addAll(deck.get(6))
            print("Cards in hand: ")
            hands.forEach { print("${hands.indexOf(it) + 1})$it ") }
            println()
            do {
                println("Choose a card to play (1-${hands.size}):")
                input = readln().lowercase()
            } while (!("[1-${hands.size}]|exit".toRegex().matches(input)))
            if (input == "exit") break
            val numberOfCards = input.toInt()
            if (table.isNotEmpty() && (hands[numberOfCards - 1].rank == table.last().rank ||
                        hands[numberOfCards - 1].suit == table.last().suit
                        )
            ) {
                println("Player wins cards")
                winFirst = true
                table.add(hands[numberOfCards - 1])
                winHands.addAll(table)
                printScore(winHands, winComputer, true)
                table.clear()
            } else {
                table.add(hands[numberOfCards - 1])
            }
            hands.removeAt(numberOfCards - 1)
        } else {
            if (computer.isEmpty()) computer.addAll(deck.get(6))
            computer.forEach { print("$it ") }
            println()
            val playComputerCard = playComputer(computer, table)
            println("Computer plays $playComputerCard")
            if (table.isNotEmpty() && (playComputerCard.rank == table.last().rank ||
                        playComputerCard.suit == table.last().suit
                        )
            ) {
                println("Computer wins cards")
                winFirst = false
                table.add(playComputerCard)
                winComputer.addAll(table)
                printScore(winHands, winComputer, false)
                table.clear()
            } else {
                table.add(playComputerCard)
            }
            computer.remove(playComputerCard)
        }
        playFirst = !playFirst
        println()
        println(
            if (table.isEmpty()) "No cards on the table"
            else "${table.size} cards on the table, and the top card is ${table.last()}"
        )
    }
    if (table.isNotEmpty()) {
        if (winFirst) winHands.addAll(table) else winComputer.addAll(table)
    }
    if (input != "exit") printScore(winHands, winComputer, playFirst, true)
    println("Game Over")
}

fun playComputer(computer: MutableList<Card>, tableCard: MutableList<Card>): Card {
    if (computer.size == 1) return computer.first()
    val topCard: Card
    var candidateCards = listOf<Card>()
    if (tableCard.isNotEmpty()) {
        topCard = tableCard.last()
        candidateCards = computer.filter { topCard.suit == it.suit || topCard.rank == it.rank }
        if (candidateCards.size == 1) return candidateCards.random()
    }
    if (tableCard.isEmpty() || candidateCards.isEmpty()) {
        Suit.values().forEach { suit ->
            val suitCards = computer.filter { card -> card.suit == suit }
            if (suitCards.size > 1) return suitCards.random()
        }
        Rank.values().forEach { rank ->
            val rankCards = computer.filter { card -> card.rank == rank }
            if (rankCards.size > 1) return rankCards.random()
        }
        return computer.random()
    }

    Suit.values().forEach { suit ->
        val suitCards = candidateCards.filter { card -> card.suit == suit }
        if (suitCards.size > 1) return suitCards.random()
    }
    Rank.values().forEach { rank ->
        val rankCards = candidateCards.filter { card -> card.rank == rank }
        if (rankCards.size > 1) return rankCards.random()
    }
    return candidateCards.random()
}

fun printScore(hands: MutableList<Card>, computer: MutableList<Card>, playFirst: Boolean, add3: Boolean = false) {
    val scoreHands = calcScore(hands) + if (hands.size > computer.size && add3) 3
    else if (hands.size == computer.size && playFirst && add3) 3 else 0
    val scoreComp = calcScore(computer) + if (hands.size < computer.size && add3) 3
    else if (hands.size == computer.size && !playFirst &&add3) 3 else 0
    println("Score: Player $scoreHands - Computer $scoreComp")
    println("Cards: Player ${hands.size} - Computer ${computer.size}")
}

fun calcScore(deck: MutableList<Card>): Int {
    var result = 0
    deck.forEach {
        if (it.rank == Rank.ACE ||
            it.rank == Rank.TEN ||
            it.rank == Rank.JACK ||
            it.rank == Rank.QUEEN ||
            it.rank == Rank.KING
        ) result++
    }
    return result
}