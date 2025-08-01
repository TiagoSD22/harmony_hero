package com.guitarvisualizer.repository

import com.guitarvisualizer.database.DatabaseConnection
import com.guitarvisualizer.models.{Chord, ChordQuality, ChordVariation}
import org.slf4j.LoggerFactory

import java.sql.{PreparedStatement, ResultSet}
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try, Using}

class ChordRepository(db: DatabaseConnection) {
  private val logger = LoggerFactory.getLogger(getClass)
  
  def findChordsByKey(key: String): Try[List[Chord]] = {
    val sql = """
      SELECT c.representation, q.name as quality, v.name as variation_name, v.diagram, v.difficulty_level
      FROM chords c
      JOIN keys k ON c.key_id = k.id
      JOIN qualities q ON c.quality_id = q.id
      JOIN variations v ON c.id = v.chord_id
      WHERE k.name = ?
      ORDER BY q.name, v.name
    """
    
    Try {
      Using(db.getConnection) { conn =>
        Using(conn.prepareStatement(sql)) { stmt =>
          stmt.setString(1, key.toUpperCase)
          val rs = stmt.executeQuery()
          
          val chordMap = scala.collection.mutable.Map[String, ListBuffer[ChordVariation]]()
          var chordRepresentations = scala.collection.mutable.Map[String, String]()
          
          while (rs.next()) {
            val quality = rs.getString("quality")
            val representation = rs.getString("representation")
            val variationName = rs.getString("variation_name")
            val diagram = rs.getString("diagram")
            val difficultyLevel = Option(rs.getInt("difficulty_level")).filter(_ != 0)
            
            chordRepresentations(quality) = representation
            chordMap.getOrElseUpdate(quality, ListBuffer.empty) += 
              ChordVariation(variationName, diagram, difficultyLevel)
          }
          
          chordMap.map { case (quality, variations) =>
            Chord(key, quality, chordRepresentations(quality), variations.toList)
          }.toList
        }.get
      }.get
    }
  }
  
  def findChordsByKeyAndQuality(key: String, quality: String): Try[Option[Chord]] = {
    val sql = """
      SELECT c.representation, v.name as variation_name, v.diagram, v.difficulty_level
      FROM chords c
      JOIN keys k ON c.key_id = k.id
      JOIN qualities q ON c.quality_id = q.id
      JOIN variations v ON c.id = v.chord_id
      WHERE k.name = ? AND q.name = ?
      ORDER BY v.name
    """
    
    Try {
      Using(db.getConnection) { conn =>
        Using(conn.prepareStatement(sql)) { stmt =>
          stmt.setString(1, key.toUpperCase)
          stmt.setString(2, quality.toLowerCase)
          val rs = stmt.executeQuery()
          
          val variations = ListBuffer[ChordVariation]()
          var representation: Option[String] = None
          
          while (rs.next()) {
            if (representation.isEmpty) {
              representation = Some(rs.getString("representation"))
            }
            
            val variationName = rs.getString("variation_name")
            val diagram = rs.getString("diagram")
            val difficultyLevel = Option(rs.getInt("difficulty_level")).filter(_ != 0)
            
            variations += ChordVariation(variationName, diagram, difficultyLevel)
          }
          
          representation.map(rep => Chord(key, quality, rep, variations.toList))
        }.get
      }.get
    }
  }
  
  def findAllQualities(): Try[List[ChordQuality]] = {
    val sql = "SELECT name, display_name FROM qualities ORDER BY name"
    
    Try {
      Using(db.getConnection) { conn =>
        Using(conn.prepareStatement(sql)) { stmt =>
          val rs = stmt.executeQuery()
          val qualities = ListBuffer[ChordQuality]()
          
          while (rs.next()) {
            val name = rs.getString("name")
            val displayName = rs.getString("display_name")
            qualities += ChordQuality(name, displayName)
          }
          
          qualities.toList
        }.get
      }.get
    }
  }
  
  def insertKey(keyName: String): Try[Int] = {
    val sql = "INSERT INTO keys (name) VALUES (?) ON CONFLICT (name) DO NOTHING RETURNING id"
    
    Try {
      Using(db.getConnection) { conn =>
        Using(conn.prepareStatement(sql)) { stmt =>
          stmt.setString(1, keyName.toUpperCase)
          val rs = stmt.executeQuery()
          if (rs.next()) {
            rs.getInt("id")
          } else {
            // Key already exists, get its ID
            findKeyId(keyName).get
          }
        }.get
      }.get
    }
  }
  
  def insertQuality(name: String, displayName: String): Try[Int] = {
    val sql = "INSERT INTO qualities (name, display_name) VALUES (?, ?) ON CONFLICT (name) DO NOTHING RETURNING id"
    
    Try {
      Using(db.getConnection) { conn =>
        Using(conn.prepareStatement(sql)) { stmt =>
          stmt.setString(1, name.toLowerCase)
          stmt.setString(2, displayName)
          val rs = stmt.executeQuery()
          if (rs.next()) {
            rs.getInt("id")
          } else {
            // Quality already exists, get its ID
            findQualityId(name).get
          }
        }.get
      }.get
    }
  }
  
  def insertChord(keyId: Int, qualityId: Int, representation: String): Try[Int] = {
    val sql = "INSERT INTO chords (key_id, quality_id, representation) VALUES (?, ?, ?) ON CONFLICT (key_id, quality_id) DO NOTHING RETURNING id"
    
    Try {
      Using(db.getConnection) { conn =>
        Using(conn.prepareStatement(sql)) { stmt =>
          stmt.setInt(1, keyId)
          stmt.setInt(2, qualityId)
          stmt.setString(3, representation)
          val rs = stmt.executeQuery()
          if (rs.next()) {
            rs.getInt("id")
          } else {
            // Chord already exists, get its ID
            findChordId(keyId, qualityId).get
          }
        }.get
      }.get
    }
  }
  
  def insertVariation(chordId: Int, variation: ChordVariation): Try[Unit] = {
    val sql = "INSERT INTO variations (chord_id, name, diagram, difficulty_level) VALUES (?, ?, ?, ?)"
    
    Try {
      Using(db.getConnection) { conn =>
        Using(conn.prepareStatement(sql)) { stmt =>
          stmt.setInt(1, chordId)
          stmt.setString(2, variation.name)
          stmt.setString(3, variation.diagram)
          stmt.setObject(4, variation.difficultyLevel.orNull)
          stmt.executeUpdate()
        }.get
      }.get
      ()
    }
  }
  
  def countChords(): Try[Int] = {
    val sql = "SELECT COUNT(*) FROM chords"
    
    Try {
      Using(db.getConnection) { conn =>
        Using(conn.prepareStatement(sql)) { stmt =>
          val rs = stmt.executeQuery()
          rs.next()
          rs.getInt(1)
        }.get
      }.get
    }
  }
  
  private def findKeyId(keyName: String): Try[Int] = {
    val sql = "SELECT id FROM keys WHERE name = ?"
    
    Try {
      Using(db.getConnection) { conn =>
        Using(conn.prepareStatement(sql)) { stmt =>
          stmt.setString(1, keyName.toUpperCase)
          val rs = stmt.executeQuery()
          if (rs.next()) {
            rs.getInt("id")
          } else {
            throw new RuntimeException(s"Key not found: $keyName")
          }
        }.get
      }.get
    }
  }
  
  private def findQualityId(qualityName: String): Try[Int] = {
    val sql = "SELECT id FROM qualities WHERE name = ?"
    
    Try {
      Using(db.getConnection) { conn =>
        Using(conn.prepareStatement(sql)) { stmt =>
          stmt.setString(1, qualityName.toLowerCase)
          val rs = stmt.executeQuery()
          if (rs.next()) {
            rs.getInt("id")
          } else {
            throw new RuntimeException(s"Quality not found: $qualityName")
          }
        }.get
      }.get
    }
  }
  
  private def findChordId(keyId: Int, qualityId: Int): Try[Int] = {
    val sql = "SELECT id FROM chords WHERE key_id = ? AND quality_id = ?"
    
    Try {
      Using(db.getConnection) { conn =>
        Using(conn.prepareStatement(sql)) { stmt =>
          stmt.setInt(1, keyId)
          stmt.setInt(2, qualityId)
          val rs = stmt.executeQuery()
          if (rs.next()) {
            rs.getInt("id")
          } else {
            throw new RuntimeException(s"Chord not found for key_id: $keyId, quality_id: $qualityId")
          }
        }.get
      }.get
    }
  }
}
