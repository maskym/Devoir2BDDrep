import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer
import scala.io.{BufferedSource, Source}


object MainObj extends App {
  val conf = new SparkConf()
    .setAppName("SparkTester")
    .setMaster("local[*]")
  val sc = new SparkContext(conf)
  sc.setLogLevel("ERROR")

  var first_id = 1
  var num_spells = 1975
  var num_monsters = 2955

  var listSpells = get_n_spells(num_spells,first_id)
  val listMonsters = get_n_monsters(num_monsters,first_id)
  println("crawler OK, début RDD")

  var listSpellsWithMonsters = matchSpellsWithMonsters(listSpells,listMonsters)
  println("RDD spells OK")

  printSpellsWithMonsters(listSpellsWithMonsters)
  println("Print spells OK")

  // AFFICHAGE ET DEBUGS
  val test = 0


  /*for (i <- 1 until num_spells) {
    println(listSpells(i).name)
  }*/


  // FONCTIONS

  def get_n_spells(n:Integer,first_id:Integer): ListBuffer[Spell] ={
    var listSpells = new ListBuffer[Spell]
    val url_base = "file:///C:/Users/Gab/IdeaProjects/Devoir2BDDrep_data/spells/spell_"
    val url_end = ".html"
    for(i <- 0 until n ){
      val html = Source.fromURL(url_base+(first_id+i)+url_end)
      val s = html.mkString
      var spell = new Spell(s, first_id+i)
      listSpells += spell
    }
    listSpells
  }

  def get_n_monsters(n:Integer,first_id:Integer): ListBuffer[Monster] ={
    var listMonsters = new ListBuffer[Monster]
    val url_base = "file:///C:/Users/Gab/IdeaProjects/Devoir2BDDrep_data/monsters/MDB_MonsterBlock.asp%23003FMDBID="
    val url_end = ".html"
    var html:BufferedSource = null
    var s = ""
    for(i <- 0 until n ){
      try {
        html = Source.fromURL(url_base+(first_id+i)+url_end)
        s = html.mkString
      } catch {
        case e: Exception => println("La page html est introuvable. " + e) }
      var monster = new Monster(s, first_id+i)
      listMonsters += monster
      println(i+1)
    }
    listMonsters
  }

  def matchSpellsWithMonsters(listSpells : ListBuffer[Spell], listMonsters : ListBuffer[Monster]): Array[Spell] ={
    val spellsRDD = sc.makeRDD(listSpells)
    val resultRDD = spellsRDD.map(current_spell =>{
      for(j <- listMonsters.indices){
        for(k <- listMonsters(j).monsterSpells.indices){
          if(current_spell.name.toLowerCase == listMonsters(j).monsterSpells(k)){
            current_spell.spellMonsters.append(listMonsters(j).name)
          }
        }
      }
      current_spell
    })
    val listSpellsWithMonsters = resultRDD.collect()
    listSpellsWithMonsters
  }

  def printSpellsWithMonsters(arraySpells : Array[Spell]): Unit ={
    for(i <- arraySpells.indices) {
      print(arraySpells(i).name + " : ")
      for(j <- arraySpells(i).spellMonsters.indices){
        print(arraySpells(i).spellMonsters(j) + " - ")
      }
      println("\n")
    }
  }
}
