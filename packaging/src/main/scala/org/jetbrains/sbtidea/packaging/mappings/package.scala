package org.jetbrains.sbtidea.packaging

import org.jetbrains.sbtidea.packaging.structure.PackagedProjectNode
import org.jetbrains.sbtidea.packaging.structure.PackagingMethod
import org.jetbrains.sbtidea.structure._

package object mappings {

  implicit class ProjectNodeExt(val node: PackagedProjectNode) extends AnyVal {
    def mmd: MappingMetaData =
      MappingMetaData(shading = node.packagingOptions.shadePatterns,
        excludeFilter = node.packagingOptions.excludeFilter,
        static = true,
        project = Some(node.name),
        kind = MAPPING_KIND.UNDEFINED)

    private def collectNodes(node: PackagedProjectNode)(predicate: PackagedProjectNode => Boolean): Seq[ProjectNode] = {
      val lst = node.parents.filter(predicate)
      lst ++ node.parents.flatMap(collectNodes(_)(predicate))
    }

    def collectStandaloneParents: Seq[ProjectNode] = collectNodes(node) {
      _.packagingOptions.packageMethod.isInstanceOf[PackagingMethod.Standalone]
    }

    /**
      * @return true if node has any parent nodes with classes
      */
    def hasRealParents: Boolean = collectNodes(node) { n =>
      n.packagingOptions.packageMethod match {
        case _: PackagingMethod.Skip | _:PackagingMethod.DepsOnly => false
        case _ => true
      }
    }.nonEmpty
  }

}
