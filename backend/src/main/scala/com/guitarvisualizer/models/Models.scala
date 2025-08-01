package com.guitarvisualizer.models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

case class ChordVariation(
  name: String,
  diagram: String,
  difficultyLevel: Option[Int] = None
)

case class Chord(
  key: String,
  quality: String,
  representation: String,
  variations: List[ChordVariation]
)

case class ChordQuality(
  name: String,
  displayName: String
)

case class ApiResponse[T](
  success: Boolean,
  data: Option[T] = None,
  error: Option[String] = None
)

case class HealthStatus(
  status: String,
  timestamp: Long,
  version: String = "1.0.0"
)

// Circe JSON encoders/decoders
object JsonCodecs {
  implicit val chordVariationEncoder: Encoder[ChordVariation] = deriveEncoder
  implicit val chordVariationDecoder: Decoder[ChordVariation] = deriveDecoder
  
  implicit val chordEncoder: Encoder[Chord] = deriveEncoder
  implicit val chordDecoder: Decoder[Chord] = deriveDecoder
  
  implicit val chordQualityEncoder: Encoder[ChordQuality] = deriveEncoder
  implicit val chordQualityDecoder: Decoder[ChordQuality] = deriveDecoder
  
  implicit def apiResponseEncoder[T: Encoder]: Encoder[ApiResponse[T]] = deriveEncoder
  implicit def apiResponseDecoder[T: Decoder]: Decoder[ApiResponse[T]] = deriveDecoder
  
  implicit val healthStatusEncoder: Encoder[HealthStatus] = deriveEncoder
  implicit val healthStatusDecoder: Decoder[HealthStatus] = deriveDecoder
}
