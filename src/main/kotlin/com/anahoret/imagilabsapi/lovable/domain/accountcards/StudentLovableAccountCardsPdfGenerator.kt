package com.anahoret.imagilabsapi.lovable.domain.accountcards

import com.anahoret.imagilabsapi.lovable.domain.LovableAccount
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*

interface StudentLovableAccountCardsPdfGenerator {

    fun generate(studentLovableAccounts: List<LovableAccount>): InputStream
}

@Service
class StudentLovableAccountCardsPdfGeneratorImpl : StudentLovableAccountCardsPdfGenerator {

    companion object {

        const val ROWS = 5
        const val COLS = 2
        const val PADDING = 20f
    }

    override fun generate(studentLovableAccounts: List<LovableAccount>): InputStream {
        val cardsPerPage = ROWS * COLS

        val totalCards = studentLovableAccounts.size
        val pageCount = totalCards / cardsPerPage + if (totalCards % cardsPerPage == 0) 0 else 1

        val document = PDDocument()

        val montseratFont = PDType0Font.load(
            document,
            ClassPathResource("fonts/Montserrat-SemiBold.ttf").inputStream
        )

        val imagiLogo = PDImageXObject.createFromByteArray(
            document,
            ClassPathResource("images/lovable_logo.png").inputStream.readAllBytes(),
            "images/lovable_logo.png"
        )

        val sayNoEvilEmoji = PDImageXObject.createFromByteArray(
            document,
            ClassPathResource("images/say_no_evil.png").inputStream.readAllBytes(),
            "images/say_no_evil.png"
        )

        for (pageIdx in 0 until pageCount) {
            val cards = studentLovableAccounts
                .drop(cardsPerPage * pageIdx)
                .take(cardsPerPage)
            addCardsPage(document, cards, montseratFont, imagiLogo, sayNoEvilEmoji)
        }

        val byteOutStream = ByteArrayOutputStream()
        document.save(byteOutStream)
        document.close()

        return byteOutStream.toByteArray().inputStream()
    }

    private fun addCardsPage(
        document: PDDocument,
        studentLovableAccounts: List<LovableAccount>,
        textFont: PDFont,
        imagiLogo: PDImageXObject,
        sayNoEvilEmoji: PDImageXObject
    ) {
        val page = PDPage()
        document.addPage(page)
        val mediaBox = page.mediaBox
        val cardHeight = (mediaBox.height - (ROWS + 1) * PADDING) / ROWS
        val cardWidth = (mediaBox.width - (COLS + 1) * PADDING) / COLS
        val pageContentStream = PDPageContentStream(document, page)

        studentLovableAccounts.forEachIndexed { cardIdx, card ->
            val i = ROWS - 1 - cardIdx / COLS
            val j = cardIdx % COLS
            val x = j * cardWidth + PADDING * (j + 1)
            val y = i * cardHeight + PADDING * (i + 1)
            addCard(
                pageContentStream,
                card,
                PDRectangle(x, y, cardWidth, cardHeight),
                textFont,
                imagiLogo,
                sayNoEvilEmoji
            )
        }
        pageContentStream.close()
    }

    private fun addCard(
        stream: PDPageContentStream,
        account: LovableAccount,
        rectangle: PDRectangle,
        textFont: PDFont,
        imagiLogo: PDImageXObject,
        sayNoEvilEmoji: PDImageXObject
    ) {
        val cardCenterX = (rectangle.lowerLeftX + rectangle.upperRightX) / 2
        drawBorder(stream, rectangle)
        drawlovableLink(stream, textFont, cardCenterX, rectangle)
        drawCredentials(stream, textFont, cardCenterX, rectangle, account)
        drawDisclaimer(stream, textFont, cardCenterX, rectangle, sayNoEvilEmoji)
        drawLogoImage(stream, imagiLogo, rectangle)
    }

    private fun drawLogoImage(
        stream: PDPageContentStream,
        imagiLogo: PDImageXObject,
        rectangle: PDRectangle
    ) {
        val imageSize = 45f
        stream.drawImage(
            imagiLogo,
            rectangle.lowerLeftX + 35f,
            rectangle.lowerLeftY + 50f,
            imageSize,
            imageSize
        )
    }

    private fun drawDisclaimer(
        stream: PDPageContentStream,
        textFont: PDFont,
        cardCenterX: Float,
        rectangle: PDRectangle,
        sayNoEvilEmoji: PDImageXObject
    ) {
        val disclaimerFontSize = 10f
        stream.setFont(textFont, disclaimerFontSize)

        stream.beginText()

        val disclaimerLine1 = "This is your top secret login information,"
        val disclaimerLine1Width = textFont.getStringWidth(disclaimerLine1) / 1000 * disclaimerFontSize
        val disclaimerLine1X = cardCenterX - disclaimerLine1Width / 2
        val disclaimerLine1Y = rectangle.lowerLeftY + 30
        stream.newLineAtOffset(disclaimerLine1X, disclaimerLine1Y)
        stream.showText(disclaimerLine1)
        stream.endText()

        stream.beginText()
        val disclaimerLine2 = "make sure not to share it with anyone!"
        val disclaimerLine2Width = textFont.getStringWidth(disclaimerLine2) / 1000 * disclaimerFontSize
        val disclaimerLine2X = cardCenterX - disclaimerLine2Width / 2
        val disclaimerLine2Y = rectangle.lowerLeftY + 15
        stream.newLineAtOffset(disclaimerLine2X, disclaimerLine2Y)
        stream.showText(disclaimerLine2)
        stream.endText()

        stream.drawImage(
            sayNoEvilEmoji,
            disclaimerLine2X + disclaimerLine2Width + 3f,
            disclaimerLine2Y - 1,
            10f,
            10f
        )
    }

    private fun drawCredentials(
        stream: PDPageContentStream,
        textFont: PDFont,
        cardCenterX: Float,
        rectangle: PDRectangle,
        account: LovableAccount
    ) {
        stream.beginText()
        val credentialsFontSize = 10f
        stream.setFont(textFont, credentialsFontSize)
        stream.setLeading(14.5f)

        val credentialsX = cardCenterX - 45
        val credentialsY = rectangle.upperRightY - 50

        stream.newLineAtOffset(credentialsX, credentialsY)
        stream.showText("Student: ${account.username}")
        stream.newLine()
        stream.showText("Username: ${account.email}")
        stream.newLine()
        stream.showText("Password: ${account.password}")
        stream.endText()
    }

    private fun drawlovableLink(
        stream: PDPageContentStream,
        textFont: PDFont,
        cardCenterX: Float,
        rectangle: PDRectangle
    ) {
        stream.beginText()
        val lovableLinkFontSize = 15f
        stream.setFont(textFont, lovableLinkFontSize)

        val lovableLink = "https://lovable.dev/login"
        val lovableLinkWidth = textFont.getStringWidth(lovableLink) / 1000 * lovableLinkFontSize
        val lovableLinkX = cardCenterX - lovableLinkWidth / 2
        val lovableLinkY = rectangle.upperRightY - 25
        stream.newLineAtOffset(lovableLinkX, lovableLinkY)
        stream.showText(lovableLink)
        stream.endText()
    }

    private fun drawBorder(
        stream: PDPageContentStream,
        rectangle: PDRectangle
    ) {
        stream.setStrokingColor(Color.DARK_GRAY)
        stream.setLineWidth(3f)
        stream.addRect(rectangle.lowerLeftX, rectangle.lowerLeftY, rectangle.width, rectangle.height)
        stream.stroke()
    }

}

fun main() {
    val pdfGenerator = StudentLovableAccountCardsPdfGeneratorImpl()
    pdfGenerator.generate(
        listOf(
            LovableAccount(UUID.randomUUID(), "Elise L", "4AXYZ1", "crystal-onion-clay"),
            LovableAccount(UUID.randomUUID(), "Sigrid", "4AXYZ2", "01234567890123456789"),
        )
    ).transferTo(FileOutputStream("student-cards.pdf"))
}
