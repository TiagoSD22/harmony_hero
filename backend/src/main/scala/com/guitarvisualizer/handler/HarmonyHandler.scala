package com.guitarvisualizer.handler

import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import com.guitarvisualizer.service.{LLMHarmonyService, HarmonyAnalysis}
import com.guitarvisualizer.models.{HarmonyRequest, HarmonyResponse}
import scala.util.{Try, Success, Failure}

class HarmonyHandler(llmService: LLMHarmonyService) {
  
  def handleHarmonyRequest(requestBody: String): Try[String] = {
    decode[HarmonyRequest](requestBody) match {
      case Right(request) =>
        llmService.analyzeHarmony(request.text_input) match {
          case Success(analysis: HarmonyAnalysis) =>
            val response = HarmonyResponse(success = true, data = Some(analysis))
            Success(response.asJson.noSpaces)
          case Failure(ex) =>
            val response = HarmonyResponse(success = false, error = Some(ex.getMessage))
            Success(response.asJson.noSpaces)
        }
      case Left(ex) =>
        val response = HarmonyResponse(success = false, error = Some(s"Invalid request format: ${ex.getMessage}"))
        Success(response.asJson.noSpaces)
    }
  }
}
