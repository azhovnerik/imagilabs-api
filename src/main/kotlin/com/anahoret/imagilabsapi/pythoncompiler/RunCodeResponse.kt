package com.anahoret.imagilabsapi.pythoncompiler

import com.fasterxml.jackson.annotation.JsonProperty

class RunCodeResponse(
    var output: CodeResult,
    var errors: List<List<String>>
)

class CodeResult(
    @field:JsonProperty("animation") val animation: Animation,
    @field:JsonProperty("scrolling_text") val scrollingText: ScrollingText,
    @field:JsonProperty("outdoor_mode") val outdoorMode: Boolean
)

class FrameObject(
    @field:JsonProperty("duration") val duration: Int,
    @field:JsonProperty("frame") val frame: String
)

class Animation(
    @field:JsonProperty("frames") val frames: List<FrameObject>,
    @field:JsonProperty("num_frames") val numFrames: Int,
    @field:JsonProperty("loop_count") val loopCount: Int
)

class ScrollingText(
    @field:JsonProperty("text") val text: String?,
    @field:JsonProperty("text_color") val textColor: String?,
    @field:JsonProperty("back_color") val backColor: String?,
    @field:JsonProperty("duration") val duration: Int?,
    @field:JsonProperty("loop_count") val loopCount: Int?
)
