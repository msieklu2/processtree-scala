package edu.luc.etl.osdi.processtree.scala.common

import java.io.{ BufferedWriter, OutputStreamWriter }

import scala.collection.JavaConversions.enumerationAsScalaIterator
import scala.math.max

/** I/O methods for the console applications. */
trait IO {

  /**
   * Parses the header row of the output of the ps command
   * and returns a function that parses subsequent lines
   * into a triple containing PID, PPID, and command string.
   *
   * @return The function for parsing subsequent lines
   */
  def parseLine(header: String): (String) => Process = {
    val cols = new java.util.StringTokenizer(header).toList
    val iPid = cols indexOf "PID"
    val iPpid = cols indexOf "PPID"
    val iCmd = max(header indexOf "CMD", header indexOf "COMMAND")
    require(iPid >= 0, "required header field PID missing!")
    require(iPpid >= 0, "required header field PPID missing!")
    require(iCmd > max(iPid, iPpid), "required header field CMD or COMMAND missing or not last!")
    (line: String) => {
      val sTok = new java.util.StringTokenizer(line)
      val words = (0 to max(iPid, iPpid)).map(_ => sTok.nextToken())
      (words(iPid).toInt, words(iPpid).toInt, line.substring(iCmd))
    }
  }

  /** The buffer size for the output writer. */
  val IO_BUF_SIZE = 8192

  /** A buffered output writer for efficiency. */
  implicit val stdout = new BufferedWriter(new OutputStreamWriter(System.out), IO_BUF_SIZE)

  /** Prints a map representing a process tree. */
  def printTree(processTree: ProcessTree)(implicit out: BufferedWriter): Unit = {
    printTree(processTree, 0, 0)(out)
    out.flush()
  }

  /** Recursively prints a map representing a process tree with indentation. */
  def printTree(processTree: ProcessTree, pid: Int, indent: Int)(implicit out: BufferedWriter): Unit = {
    for (children <- processTree.get(pid); (cpid, _, cmd) <- children) {
      for (_ <- 1 to indent) out.append(' ')
      out.append(cpid.toString)
      out.append(": ")
      out.append(cmd)
      out.newLine()
      printTree(processTree, cpid, indent + 1)(out)
    }
  }
}
