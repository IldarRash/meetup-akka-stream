package com.example.helloworld


import akka.NotUsed
import akka.stream.scaladsl.Source

import java.io.File
import scala.io.Source.fromFile

class FileService {

  def getAllFilesFromDir(pathDir : String): Iterator[File] =
    new File(getClass.getResource(pathDir).getPath).listFiles.filter(_.isFile).toList.iterator


  def getAllStringFromFile(file: File): Source[String, NotUsed] =
    Source.fromIterator(() => fromFile(file).getLines())
}
