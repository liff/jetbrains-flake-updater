{
  description = "Updater for JetBrains flake";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    devshell.url = "github:numtide/devshell";
    devshell.inputs.nixpkgs.follows = "nixpkgs";
    flake-utils.url = "github:numtide/flake-utils";
    sbt.url = "github:zaninime/sbt-derivation";
    sbt.inputs.nixpkgs.follows = "nixpkgs";
  };

  outputs = { self, devshell, flake-utils, sbt, nixpkgs }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          overlays = [ devshell.overlay ];
        };
        graalvm = pkgs.graalvm17-ce;
        pname = "jetbrains-flake-updater";
      in rec {
        packages.default = sbt.lib.mkSbtDerivation {
          inherit pkgs pname;

          version = "1";

          depsSha256 = "sha256-q6CMezUPwE3WnNB01HxJisg6W7+Kw0B/lgjfXrZttxk=";

          src = ./.;

          nativeBuildInputs = [ graalvm ];

          buildPhase = ''
            runHook preBuild
            sbt nativeImage
            runHook postBuild
          '';

          installPhase = ''
            runHook preInstall
            mkdir -p "$out/bin"
            cp -a target/native-image/${pname} "$out/bin/"
            runHook postInstall
          '';
        };

        apps.default = flake-utils.lib.mkApp { drv = packages.default; };

        devShells.default =
          pkgs.devshell.mkShell {
            packages = with pkgs; [
              sbt
              graalvm
              dotty
            ];
          };
      });
}
