package com.example

import com.example.ai.AiService
import com.example.ai.CommandActionType
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  private val aiService = AiService()

  @Test
  fun testCommandParsing() {
    val cmdYouTube = aiService.parseUserCommand("Ayesha, open YouTube please")
    assertEquals(CommandActionType.OPEN_YOUTUBE, cmdYouTube.actionType)

    val cmdCamera = aiService.parseUserCommand("Open camera")
    assertEquals(CommandActionType.OPEN_CAMERA, cmdCamera.actionType)

    val cmdSettings = aiService.parseUserCommand("Open settings")
    assertEquals(CommandActionType.OPEN_SETTINGS, cmdSettings.actionType)

    val cmdStop = aiService.parseUserCommand("Stop speaking")
    assertEquals(CommandActionType.STOP_SPEAKING, cmdStop.actionType)

    val cmdGeneral = aiService.parseUserCommand("Tell me about the galaxy")
    assertEquals(CommandActionType.NONE, cmdGeneral.actionType)
  }
}
