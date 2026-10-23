// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_shared",
  platforms: [
    .iOS("17.6")
  ],
  products: [
    .library(
      name: "_shared",
      type: .none,
      targets: ["_shared"]
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
    ),
    .package(
      url: "https://github.com/pytorch/executorch.git",
      branch: "swiftpm-1.5.0"
    )
  ],
  targets: [
    .target(
      name: "_shared",
      dependencies: [
        .product(
          name: "LiteRTLM",
          package: "LiteRT-LM"
        ),
        .product(
          name: "DotLottie",
          package: "dotlottie-ios"
        ),
        .product(
          name: "executorch",
          package: "executorch"
        ),
        .product(
          name: "backend_xnnpack",
          package: "executorch"
        ),
        .product(
          name: "backend_coreml",
          package: "executorch"
        ),
        .product(
          name: "backend_mlx",
          package: "executorch"
        )
      ]
    )
  ]
)
