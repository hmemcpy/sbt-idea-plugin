package org.jetbrains.sbtidea.download.jbr

import java.nio.file.{Files, Path}
import java.util.Properties

import org.jetbrains.sbtidea.download.api._
import org.jetbrains.sbtidea.download.{FileDownloader, NioUtils}
import org.jetbrains.sbtidea.packaging.artifact.using
import org.jetbrains.sbtidea.{pathToPathExt, PluginLogger => log, _}
import org.rauschig.jarchivelib.{ArchiveFormat, ArchiverFactory, CompressionType}
import sbt._

class JbrInstaller extends Installer[JbrArtifact] {
  import JbrInstaller._

  override def isInstalled(art: JbrArtifact)(implicit ctx: InstallContext): Boolean =
    (ctx.baseDirectory / JBR_DIR_NAME).exists

  override def downloadAndInstall(art: JbrArtifact)(implicit ctx: InstallContext): Unit = {
    val file = FileDownloader(ctx.downloadDirectory).download(art.dlUrl)
    install(file)
  }

  private[jbr] def install(dist: Path)(implicit ctx: InstallContext): Unit = {
    val archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP)
    val tmpDir = Files.createTempDirectory(ctx.baseDirectory, "jbr-extract")
    log.info(s"extracting jbr to $tmpDir")
    archiver.extract(dist.toFile, tmpDir.toFile)
    val installPath = ctx.baseDirectory / JBR_DIR_NAME
    val children = tmpDir.list
    if (children.size == 1) {
      NioUtils.delete(installPath)
      Files.move(children.head, installPath)
      NioUtils.delete(tmpDir)
      log.info(s"installed JBR into $installPath")
    } else {
      log.error(s"Unexpected JBR archive structure, expected single directory")
    }
  }
}

object JbrInstaller {
  val JBR_DIR_NAME    = "jbr"
}