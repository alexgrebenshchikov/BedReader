import java.nio.file.Path
import kotlinx.serialization.*
import kotlinx.serialization.json.*


/**
 * Index store map of (chromosome name to (start of entry, end of entry, number of line in BED file) ).
 * */
data class MyBedIndex(val chromosomeMap: Map<String, List<Triple<Int, Int, Int>>>) : BedIndex

class MyBedReader : BedReader {
    override fun createIndex(bedPath: Path, indexPath: Path) {
        val chrMap: MutableMap<String, MutableList<Triple<Int, Int, Int>>> = mutableMapOf()
        var numberOfLine = 0
        bedPath.toFile().forEachLine {
            val splitLine = it.split(""" +|\t""".toRegex())
            val chrName = splitLine.firstOrNull()
            if (chrName != null && chrName != "browser" && chrName != "track") {
                if (!chrMap.containsKey(chrName))
                    chrMap[chrName] = mutableListOf()
                chrMap[chrName]?.insertionSort(Triple(splitLine[1].toInt(), splitLine[2].toInt(), numberOfLine))
            }
            numberOfLine++
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
        val listOfEntries = (index as MyBedIndex).chromosomeMap[chromosome] ?: return listOf()
        var i = binSearch(listOfEntries, start)

        val res: MutableList<BedEntry> = mutableListOf()
        val lines = bedPath.toFile().readLines().map { it.split(""" +|\t""".toRegex()).drop(3) }

        while (i < listOfEntries.size && listOfEntries[i].first < end) {
            if (listOfEntries[i].second <= end)
                res.add(
                    BedEntry(
                        chromosome,
                        listOfEntries[i].first,
                        listOfEntries[i].second,
                        lines[listOfEntries[i].third]
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
