import org.junit.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestMyBedReader {
    private val reader = MyBedReader()
    private val bedPath = Path.of("src/test/resources/example.bed")
    private val indexPath = Path.of("src/test/resources/index.json")
    init {
        reader.createIndex(bedPath,
            indexPath)
    }


    @Test
    fun testReadAndWriteIndex() {
        val index = reader.loadIndex(indexPath)
        val expected = "MyBedIndex(chromosomeMap={chr7=[(0, 127, 3), (12, 99, 11), (12, 80, 10), (12, 474, 5)]," +
                " chr3=[(12, 74, 4), (47, 274, 6), (365, 532, 15)], chr5=[(127, 270, 7)," +
                " (532, 1699, 16)], chr12=[(1, 781, 8), (81, 936, 9)], chr10=[(127, 7031, 12)], " +
                "chr16=[(12, 74798, 13), (198, 9365, 14)]})"
        assertEquals(expected, index.toString())
    }

    /**
     * Chromosome name which is missing in file.
     * */
    @Test
    fun testFindWithIndex1() {
        val index = reader.loadIndex(indexPath)
        val res = reader.findWithIndex(index, bedPath,"chr42", 0, 99)
        assertTrue(res.isEmpty())
    }

    /**
     * Chromosome name which is present in file.
     * */
    @Test
    fun testFindWithIndex2() {
        val index = reader.loadIndex(indexPath)
        val res = reader.findWithIndex(index, bedPath,"chr3", 30, 600)
        val expected  = "[BedEntry(chromosome=chr3, start=47, end=274, other=[Pos4, 0, +, 127474697, 127475864, 255,0,0]), " +
                "BedEntry(chromosome=chr3, start=365, end=532, other=[Pos5, 0, +, 127479365, 127480532, 255,0,0])]"
        assertEquals(expected, res.toString())
    }

    @Test
    fun testFindWithIndex3() {
        val index = reader.loadIndex(indexPath)
        val res = reader.findWithIndex(index, bedPath,"chr7", 12, 100)
        val expected  = "[BedEntry(chromosome=chr7, start=12, end=99, other=[Neg4, 0, -, 127480532, 127481699, 0,0,255])," +
                " BedEntry(chromosome=chr7, start=12, end=80, other=[Pos5, 0, +, 127479365, 127480532, 255,0,0])]"
        assertEquals(expected, res.toString())
    }

    /**
     * Chromosome name which is present in file, but no one of entries is located inside the given range.
     * */
    @Test
    fun testFindWithIndex4() {
        val index = reader.loadIndex(indexPath)
        val res = reader.findWithIndex(index, bedPath,"chr12", 1000, 1024)
        assertTrue(res.isEmpty())
    }

    @Test
    fun testFindWithIndex5() {
        val index = reader.loadIndex(indexPath)
        val res = reader.findWithIndex(index, bedPath,"chr16", 100, 200)
        assertTrue(res.isEmpty())
    }

}