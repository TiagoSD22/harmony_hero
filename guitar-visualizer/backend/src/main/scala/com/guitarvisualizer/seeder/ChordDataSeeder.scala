package com.guitarvisualizer.seeder

import com.guitarvisualizer.config.DatabaseConfig
import com.guitarvisualizer.database.DatabaseConnection
import com.guitarvisualizer.models.{Chord, ChordVariation}
import com.guitarvisualizer.repository.ChordRepository
import io.circe.parser._
import org.slf4j.LoggerFactory

import java.io.File
import scala.io.Source
import scala.util.{Failure, Success, Try, Using}

object ChordDataSeeder {
  private val logger = LoggerFactory.getLogger(getClass)
  
  def seedIfEmpty(config: DatabaseConfig): Unit = {
    val db = DatabaseConnection.initialize(config)
    val repository = new ChordRepository(db)
    
    repository.countChords() match {
      case Success(count) if count > 0 =>
        logger.info(s"Database already contains $count chords, skipping seeding")
      case Success(_) =>
        logger.info("Database is empty, seeding chord data...")
        seedChordData(repository)
      case Failure(ex) =>
        logger.error("Failed to check chord count", ex)
        throw ex
    }
  }
  
  private def seedChordData(repository: ChordRepository): Unit = {
    val sharedPath = "src/main/resources/chords"
    val chordFiles = List(
      "a.json", "a#.json", "b.json", "c.json", "c#.json",
      "d.json", "d#.json", "e.json", "f.json", "f#.json",
      "g.json", "g#.json"
    )
    
    var totalChords = 0
    var totalVariations = 0
    
    // First, insert all unique qualities
    val allQualities = collectAllQualities(sharedPath, chordFiles)
    allQualities.foreach { quality =>
      val displayName = formatQualityDisplayName(quality)
      repository.insertQuality(quality, displayName) match {
        case Success(_) => logger.debug(s"Inserted quality: $quality")
        case Failure(ex) => logger.warn(s"Failed to insert quality $quality: ${ex.getMessage}")
      }
    }
    
    chordFiles.foreach { fileName =>
      val keyName = extractKeyFromFileName(fileName)
      val filePath = s"$sharedPath/$fileName"
      
      logger.info(s"Processing chord file: $fileName for key: $keyName")
      
      try {
        val chords = loadChordsFromFile(filePath, keyName)
        
        // Insert key
        val keyId = repository.insertKey(keyName).get
        
        chords.foreach { chord =>
          try {
            // Insert quality if not exists
            val qualityId = repository.insertQuality(chord.quality, formatQualityDisplayName(chord.quality)).get
            
            // Insert chord
            val chordId = repository.insertChord(keyId, qualityId, chord.representation).get
            
            // Insert variations
            chord.variations.foreach { variation =>
              repository.insertVariation(chordId, variation) match {
                case Success(_) => 
                  totalVariations += 1
                case Failure(ex) => 
                  logger.warn(s"Failed to insert variation ${variation.name}: ${ex.getMessage}")
              }
            }
            
            totalChords += 1
            
          } catch {
            case ex: Exception =>
              logger.error(s"Failed to process chord ${chord.representation} in key $keyName", ex)
          }
        }
        
      } catch {
        case ex: Exception =>
          logger.error(s"Failed to process file $fileName", ex)
      }
    }
    
    logger.info(s"Seeding completed! Inserted $totalChords chords with $totalVariations variations.")
  }
  
  private def loadChordsFromFile(filePath: String, keyName: String): List[Chord] = {
    val resourcePath = if (filePath.startsWith("src/main/resources/")) {
      filePath.replace("src/main/resources/", "")
    } else {
      filePath
    }

    Option(getClass.getClassLoader.getResourceAsStream(resourcePath)) match {
      case Some(inputStream) =>
        try {
          val content = scala.io.Source.fromInputStream(inputStream, "UTF-8").mkString
          if (filePath.endsWith(".json")) {
            parseJsonChords(content, keyName)
          } else {
            parseJsonChords(content, keyName)
          }
        } catch {
          case ex: Exception =>
            logger.error(s"Error reading file $filePath: ${ex.getMessage}")
            List.empty
        } finally {
          inputStream.close()
        }
      case None =>
        logger.error(s"Resource not found: $resourcePath")
        List.empty
    }
  }
  
  private def parseJsonChords(content: String, keyName: String): List[Chord] = {
    parse(content) match {
      case Right(json) =>
        json.asArray match {
          case Some(array) =>
            // Format like c.json - array of chord objects
            array.flatMap { chordJson =>
              for {
                keyField <- chordJson.hcursor.get[String]("key").toOption
                quality <- chordJson.hcursor.get[String]("quality").toOption
                representation <- chordJson.hcursor.get[String]("representation").toOption
                variationsJson <- chordJson.hcursor.get[List[io.circe.Json]]("variations").toOption
              } yield {
                val variations = variationsJson.flatMap { varJson =>
                  for {
                    name <- varJson.hcursor.get[String]("name").toOption
                    diagram <- varJson.hcursor.get[String]("diagram").toOption
                  } yield ChordVariation(name, diagram, None)
                }
                Chord(keyField, quality, representation, variations)
              }
            }.toList
          case None =>
            // Format like b.json - object with chords array
            json.hcursor.get[List[io.circe.Json]]("chords") match {
              case Right(chordsArray) =>
                chordsArray.flatMap { chordJson =>
                  for {
                    quality <- chordJson.hcursor.get[String]("quality").toOption
                    representation <- chordJson.hcursor.get[String]("representation").toOption
                    variationsJson <- chordJson.hcursor.get[List[io.circe.Json]]("variations").toOption
                  } yield {
                    val variations = variationsJson.flatMap { varJson =>
                      for {
                        name <- varJson.hcursor.get[String]("name").toOption
                        diagram <- varJson.hcursor.get[String]("diagram").toOption
                      } yield ChordVariation(name, diagram, None)
                    }
                    Chord(keyName, quality, representation, variations)
                  }
                }
              case Left(_) =>
                logger.warn(s"Could not parse chords from JSON for key $keyName")
                List.empty
            }
        }
      case Left(error) =>
        logger.error(s"Failed to parse JSON for key $keyName: ${error.getMessage}")
        List.empty
    }
  }
  
  private def collectAllQualities(sharedPath: String, files: List[String]): Set[String] = {
    val qualities = scala.collection.mutable.Set[String]()
    
    files.foreach { fileName =>
      val keyName = extractKeyFromFileName(fileName)
      val filePath = s"$sharedPath/$fileName"
      val chords = loadChordsFromFile(filePath, keyName)
      chords.foreach(chord => qualities += chord.quality)
    }
    
    qualities.toSet
  }
  
  private def extractKeyFromFileName(fileName: String): String = {
    fileName.split("\\.").head.toUpperCase.replace("#", "#")
  }
  
  private def formatQualityDisplayName(quality: String): String = {
    quality match {
      case "major" => "Major"
      case "minor" => "Minor"
      case "dominant7" => "Dominant 7th"
      case "major7" => "Major 7th"
      case "minor7" => "Minor 7th"
      case "diminished" => "Diminished"
      case "augmented" => "Augmented"
      case "suspended4" => "Suspended 4th"
      case "suspended2" => "Suspended 2nd"
      case "add9" => "Add 9"
      case "major6" => "Major 6th"
      case "minor6" => "Minor 6th"
      case "dominant9" => "Dominant 9th"
      case "dominant11" => "Dominant 11th"
      case "dominant13" => "Dominant 13th"
      case "half-diminished" => "Half Diminished"
      case "minor7b5" => "Minor 7♭5"
      case "maj9" => "Major 9th"
      case "sus2" => "Suspended 2nd"
      case "sus4" => "Suspended 4th"
      case "6" => "6th"
      case "m6" => "Minor 6th"
      case "9" => "9th"
      case "13" => "13th"
      case other => other.capitalize
    }
  }
}
