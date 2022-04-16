package updater

import org.http4s.Uri

import Edition.*, Status.*, Variant.*

object config:
  object uris:
    val updates: Uri = uri"https://www.jetbrains.com/updates/updates.xml"

    val downloadSite: Uri = uri"https://download.jetbrains.com"

  val downloadPaths: Map[String, String] = Map(
    "CLion"         -> "cpp",
    "DataGrip"      -> "datagrip",
    "GoLand"        -> "go",
    "IntelliJ IDEA" -> "idea",
    "PhpStorm"      -> "webide",
    "PyCharm"       -> "python",
    "RubyMine"      -> "ruby",
    "WebStorm"      -> "webstorm",
  )

  val packagePrefix: PartialFunction[(String, Edition), String] =
    case ("CLion", Licensed)          => "CLion"
    case ("DataGrip", Licensed)       => "datagrip"
    case ("GoLand", Licensed)         => "goland"
    case ("IntelliJ IDEA", Licensed)  => "ideaIU"
    case ("IntelliJ IDEA", Community) => "ideaIC"
    case ("PhpStorm", Licensed)       => "PhpStorm"
    case ("PyCharm", Licensed)        => "pycharm-professional"
    case ("PyCharm", Community)       => "pycharm-community"
    case ("RubyMine", Licensed)       => "RubyMine"
    case ("WebStorm", Licensed)       => "WebStorm"

  def downloadUriFor(product: String, edition: Edition, status: Status, variant: Variant, build: Build): Option[Uri] =
    (downloadPaths.get(product), packagePrefix.lift((product, edition))).mapN { (path, prefix) =>
      val version = status.match
        case Release => build.version
        case Eap     => build.fullNumber.getOrElse(build.number)
      val vary = variant.match
        case Default => ""
        case NoJbr   => "-no-jbr"
      uris.downloadSite / path / s"$prefix-$version$vary.tar.gz"
    }
