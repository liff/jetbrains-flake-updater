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
        jdk = pkgs.openjdk17;
      in rec {
        packages.default = sbt.lib.mkSbtDerivation {
          inherit pkgs;

          pname = "jetbrains-flake-updater";
          version = "1";
 
          depsSha256 = "sha256-H6z/Gi49LasuTBWXvOMsDREbJV7mE4S+5ZUxjyJ4uXk=";
 
          src = ./.;
 
          buildPhase = ''
            runHook preBuild
            sbt stage
            runHook postBuild
          '';
 
          installPhase = ''
            runHook preInstall
            mkdir -p "$out/"
            cp -a target/universal/stage/* "$out/"
            substituteInPlace "$out/bin/jetbrains-flake-updater" \
                --replace '@OUT@' "$out" \
                --replace '@SHELL@' "${pkgs.runtimeShell}" \
                --replace '@JAVA@' "${jdk}/bin/java"
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
