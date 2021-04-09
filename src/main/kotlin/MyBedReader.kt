import java.nio.file.Path
import kotlinx.serialization.*
import kotlinx.serialization.json.*


data class MyBedIndex(val chromosomeMap: Map<String, List<Triple<Int, Int, Int>>>) : BedIndex

class MyBedReader : BedReader {
    override fun createIndex(bedPath: Path, indexPath: Path) {
        val chrMap: MutableMap<String, MutableList<Triple<Int, Int, Int>>> = mutableMapOf()
        var n = 0
        bedPath.toFile().forEachLine {
            val splitLine = it.split(""" +|\t""".toRegex())
            val chrName = splitLine.firstOrNull()
            if (chrName != null && chrName != "browser" && chrName != "track") {
                if (!chrMap.containsKey(chrName))
                    chrMap[chrName] = mutableListOf()
                chrMap[chrName]?.insertionSort(Triple(splitLine[1].toInt(), splitLine[2].toInt(), n))
            }
            n++
        }
        indexPath.toFile().writeText(Json.encodeToString(chrMap))
    }

    override fun loadIndex(indexPath: Path): BedIndex {
        val a: Map<String, List<Triple<Int, Int, Int>>> = Json.decodeFromString(
            indexPath.toFile().readText()
        )
        return MyBedIndex(a)
    }

    override fun findWithIndex(
        index: BedIndex, bedPath: Path,
        chromosome: String, start: Int, end: Int
    ): List<BedEntry> {
        val listOfPieces = (index as MyBedIndex).chromosomeMap[chromosome] ?: return listOf()
        var i = binSearch(listOfPieces, start)

        val res: MutableList<BedEntry> = mutableListOf()
        val lines = bedPath.toFile().readLines().map { it.split(""" +|\t""".toRegex()).drop(3) }

        while (i < listOfPieces.size && listOfPieces[i].first < end) {
            if (listOfPieces[i].second <= end)
                res.add(
                    BedEntry(
                        chromosome,
                        listOfPieces[i].first,
                        listOfPieces[i].second,
                        lines[listOfPieces[i].third]
                    )
                )
            i++
        }
        return res
    }

}


fun binSearch(list: List<Triple<Int, Int, Int>>, elem: Int): Int {
    var l = -1
    var r = list.size
    while (l < r - 1) {
        val m = (l + r) / 2
        if (list[m].first < elem)
            l = m
        else
            r = m
    }
    return r
}


fun MutableList<Triple<Int, Int, Int>>.insertionSort(elem: Triple<Int, Int, Int>) {
    val i = binSearch(this, elem.first)
    add(i, elem)
}
