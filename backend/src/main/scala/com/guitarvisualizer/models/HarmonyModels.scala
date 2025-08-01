package com.guitarvisualizer.models

import io.circe.*
import io.circe.generic.auto.*
import com.guitarvisualizer.service.HarmonyAnalysis

case class HarmonyRequest(text_input: String)

case class HarmonyResponse(
  success: Boolean,
  data: Option[HarmonyAnalysis] = None,
  error: Option[String] = None
)
