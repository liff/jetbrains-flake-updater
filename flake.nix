{
  description = "Updater for JetBrains flake";

  inputs = {
    devshell.url = "github:numtide/devshell";
    flake-utils.url = "github:numtide/flake-utils";
    sbt-derivation.url = "github:zaninime/sbt-derivation";
  };

  outputs = { self, devshell, flake-utils, sbt-derivation, nixpkgs }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          overlays = [ devshell.overlay sbt-derivation.overlay ];
        };
      in rec {
        packages.default =pkgs.sbt.mkDerivation {
          pname = "jetbrains-flake-updater";
          version = "1";
 
          depsSha256 = "sha256-NJ7nYpgTf7a40aPFG8z5NurWPwK226jhKfYeZ7jedlY=";
 
          src = ./.;
 
#          NATIVE_IMAGE_INSTALLED = "true";
#          GRAALVM_HOME = pkgs.graalvm17-ce;
 
          buildInputs = [ pkgs.graalvm17-ce ];

          buildPhase = ''
            runHook preBuild
            sbt graalvm-native-image:packageBin
            runHook postBuild
          '';
 
          installPhase = ''
            runHook preInstall
            mkdir -p "$out/bin"
            cp target/graalvm-native-image/jetbrains-flake-updater "$out/bin"
            runHook postInstall
          '';
        };

        apps.default = flake-utils.lib.mkApp { drv = packages.default; };

        devShell =
          pkgs.devshell.mkShell {
            packages = with pkgs; [
              sbt
              graalvm17-ce
              dotty
            ];
          };
      });
}
