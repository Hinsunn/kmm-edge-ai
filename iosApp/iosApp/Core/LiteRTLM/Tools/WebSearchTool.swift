//
// File        : WebSearchTool.swift
// Author      : Tran Van Hoai
// Date        : 20/10/26
//

import Foundation
import LiteRTLM

struct WebSearchTool: Tool {
    static let name = "web_search"
    static let description = "Search the web for information with a query and return the results."

    @ToolParam(description: "The search query to use for the web search.")
    var query: String

    // ponytail: small limit + truncated snippets because maxNumTokens is 1024; raise both together.
    private static let limit = 3
    private static let snippetLength = 300

    private struct Response: Decodable {
        struct Data: Decodable { let web: [Item]? }
        struct Item: Decodable { let url: String; let title: String?; let description: String? }
        let success: Bool
        let data: Data?
        let error: String?
    }

    // LiteRTLM aborts the whole stream when a tool throws, so failures go back to the model as data.
    func run() async throws -> Any {
        guard let apiKey = Bundle.main.object(forInfoDictionaryKey: "FIRECRAWL_API_KEY") as? String,
              !apiKey.isEmpty, !apiKey.hasPrefix("$(")
        else {
            return ["error": "Web search is not configured."]
        }

        var request = URLRequest(url: URL(string: "https://api.firecrawl.dev/v2/search")!)
        request.httpMethod = "POST"
        request.timeoutInterval = 20
        request.setValue("Bearer \(apiKey)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONSerialization.data(withJSONObject: ["query": query, "limit": Self.limit])

        do {
            let (data, _) = try await URLSession.shared.data(for: request)
            let response = try JSONDecoder().decode(Response.self, from: data)
            guard response.success else {
                return ["error": response.error ?? "Web search failed."]
            }
            return (response.data?.web ?? []).map { item in
                [
                    "title": item.title ?? "",
                    "url": item.url,
                    "snippet": String((item.description ?? "").prefix(Self.snippetLength)),
                ]
            }
        } catch {
            return ["error": "Web search failed: \(error.localizedDescription)"]
        }
    }
}
