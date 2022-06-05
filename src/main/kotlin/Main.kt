import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

data class GarageState (
    var parkingSpots: Map<Int, Int?> = mapOf(),
    var carsQueued: List<Int> = listOf(),
    var turnover: Int = 0,
)
fun main(args: Array<String>) {
    // read input file
    val level = 5
    val inputNo = 5
    var input = ClassLoader.getSystemClassLoader().getResource("level$level/input.$inputNo").readText()
    var lines = input.split("\n")
    lines = lines
        .map { it.replace("\r", "") }
        .map { it.trim() }
    var firstLine = lines[0]
    var secondLine = lines[1]
    var thirdLine = lines[2]
    var fourthLine = lines[3]

    // break down the lines of input
    var firstLineParts = firstLine.split(" ")
    var secondLineParts = secondLine.split(" ")
    var thirdLineParts = thirdLine.split(" ")
    var fourthLineParts = fourthLine.split(" ")

    // parse out the structure we are working with
    var parkingSpotsCount = firstLineParts[0].toInt()
    var carsCount = firstLineParts[1].toInt()

    var parkingSpotsPrices = secondLineParts.map { it.toInt() }

    var carWeights = thirdLineParts.map { it.toInt() }.withIndex().associate { Pair(it.index + 1, it.value) }

    var carLog = fourthLineParts.map { it.toInt() }
    assert(carLog.size == carsCount)

    // full history of states of the garage
    var parkhouseHistory = mutableListOf<GarageState>()
    parkhouseHistory.add(
        GarageState(
            parkingSpots = parkingSpotsPrices.indices.associate { index -> index to null }.toMap(),
        )
    )

    // each log entry results into a new state
    for (log in carLog) {
        var lastState = parkhouseHistory.last()

        var lastParkingSpots = lastState.parkingSpots
        var lastCarsQueued = lastState.carsQueued
        var lastTurnover = lastState.turnover

        var nextParkingSpots = lastParkingSpots.toMutableMap()
        var nextCarsQueued = lastCarsQueued.toMutableList()
        var nextTurnover = lastTurnover

        var carNumberToParkingSpotNumber = nextParkingSpots.entries.filter { it.value != null }.associate { it.value!! to it.key }

        if (log > 0) {
            // car is trying to enter the garage
            assert(nextParkingSpots.size <= parkingSpotsCount)
            var isFull = nextParkingSpots.all { it.value != null }

            // add car to queue if garage is full,
            // otherwise park it in the parking spot with the lowest number
            if (isFull) {
                nextCarsQueued.add(log)
            } else {
                var lowestFreeParkingSpot = nextParkingSpots.entries.filter { it.value == null }.minByOrNull { it.key }!!.key
                nextParkingSpots[lowestFreeParkingSpot] = log
            }
        } else {
            // car is leaving the garage
            val carNumber = -log

            // check if car is already parked or in the queue
            if (carNumberToParkingSpotNumber.containsKey(carNumber)) {
                var parkingSpotNumber = carNumberToParkingSpotNumber[carNumber]!!

                // calculate the price
                // parking spot price times the weight of the car
                var parkingSpotPrice = parkingSpotsPrices[parkingSpotNumber]
                var carWeight = carWeights[carNumber]!!
                var weightPriceMultiplicator = Math.ceil(carWeight / 100.toDouble()).toInt()
                var priceToPay = parkingSpotPrice * weightPriceMultiplicator

                nextParkingSpots[parkingSpotNumber] = null
                nextTurnover += priceToPay

                // move car from the queue into the garage
                if (nextCarsQueued.isNotEmpty()) {
                    var nextCar = nextCarsQueued.removeAt(0)
                    var lowestFreeParkingSpot =
                        nextParkingSpots.entries.filter { it.value == null }.minByOrNull { it.key }!!.key
                    nextParkingSpots[lowestFreeParkingSpot] = nextCar
                }
            } else {
                // remove car from queue
                // nothing to pay
                assert(nextCarsQueued.contains(carNumber))
                nextCarsQueued.remove(carNumber)
            }
        }
        parkhouseHistory.add(GarageState(nextParkingSpots, nextCarsQueued, nextTurnover))
    }

    var turnover = parkhouseHistory.last().turnover
    var result = "$turnover"

    /* copy result to clipboard */
    var stringSelection = StringSelection(result)
    Toolkit.getDefaultToolkit().systemClipboard.setContents(stringSelection, null)

    println(result)
}