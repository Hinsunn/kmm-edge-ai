// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "KotlinMultiplatformLinkedPackage",
  platforms: [
    .iOS("17.6")
  ],
  products: [
    .library(
      name: "KotlinMultiplatformLinkedPackage",
      type: .none,
      targets: ["KotlinMultiplatformLinkedPackage"]
    )
  ],
  dependencies: [
    .package(
      url: "https://github.com/google-ai-edge/LiteRT-LM.git",
      from: "0.18.0"
    ),
    .package(
      url: "https://github.com/LottieFiles/dotlottie-ios.git",
      from: "0.16.8"
    )
  ],
  targets: [
    .target(
      name: "KotlinMultiplatformLinkedPackage",
      dependencies: [
        .product(
          name: "LiteRTLM",
          package: "LiteRT-LM"
        ),
        .product(
          name: "DotLottie",
          package: "dotlottie-ios"
        )
      ]
    )
  ]
)
