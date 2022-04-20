package com.anahoret.imagilabsapi.pythoncompiler.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service

interface AnimatedTextGenerator {

    fun generateScrollingTextAnimation(scrollingText: ScrollingText): Animation
}

@Service
class AnimatedTextGeneratorImpl(
    private val objectMapper: ObjectMapper
) : AnimatedTextGenerator {

    /**
     * Given a codeResult object, converts the scrolling text to an animation (frames, including duration)
     */
    override fun generateScrollingTextAnimation(scrollingText: ScrollingText): Animation {
        val bigBitmap = convertTextToBigBitmap(
            scrollingText.text ?: "",
            scrollingText.textColor ?: "0",
            scrollingText.backColor ?: "0"
        )
        val frames = mutableListOf<FrameObject>()
        var runner = 0

        while (runner < bigBitmap[0].size - 8) {
            val sb = StringBuilder()
            for (r in 0..7) {
                for (c in runner until runner + 8) {
                    sb.append(bigBitmap[r][c]).append(";")
                }
            }
            frames.add(FrameObject(scrollingText.duration ?: 0, sb.toString()))
            runner++
        }
        return Animation(frames, frames.size, loopCount = 0)
    }

    /**
     * Creates a large concatenated matrix with bitmapped chars joined together.
     * Given this, it should be easy to slide a window and generate frames
     *
     * @param text    the scrolling text
     * @param fgColor foreground color for the text in the format "r,g,b"
     * @param bgColor background color in the format "r,g,b"
     * @return large concatenated matrix with bitmapped chars joined together
     */
    private fun convertTextToBigBitmap(
        text: String,
        fgColor: String,
        bgColor: String
    ): Array<Array<String>> {
        val bitmapMap = ClassPathResource("data/bitmaps.json")
            .inputStream
            .reader(Charsets.UTF_8)
            .readText()
            .let<String, Map<String, String>>(objectMapper::readValue)

        // Initialize the big bitmap to background color
        val result = Array(8) {
            Array(size = 8 + 6 * text.length + 7) { bgColor }
        }
        var start = 8
        for (character in text) {
            val bitmapString = bitmapMap[character.toString()]
            placeInBigBitmap(
                result,
                bitmapString!!,
                fgColor,
                bgColor,
                start
            )
            start += 6
        }
        return result
    }

    /**
     * Takes a bitmap string like 0,0,1,0,1.... that represents a character and places it into a concatenated color matrix
     * The 0s are replaced with bgColor and 1s are replaced with fgColor
     * Assumed that the last 2 columns of the character bitmap are padding (aka 0s)
     *
     * @param result       the large color matrix
     * @param bitmapString the bitmap string (from the json file). expected format is 0,0,1,1,0..
     * 63 commas (64 chars) in the string
     * @param fgColor      foreground color for the text in the format "r,g,b"
     * @param bgColor      background color in the format "r,g,b"
     * @param startIndex   where in the large matrix to place the new character
     */
    private fun placeInBigBitmap(
        result: Array<Array<String>>,
        bitmapString: String,
        fgColor: String,
        bgColor: String,
        startIndex: Int
    ) {
        val bwBitmapArray = bitmapString.split(",")
        var index = 0
        // limit cols till 6
        for (r in 0 until 8) {
            for (c in startIndex until startIndex + 6) {
                result[r][c] = when (bwBitmapArray[index]) {
                    "0" -> bgColor
                    else -> fgColor
                }
                index += 1
            }
            index += 2
        }
    }

}
