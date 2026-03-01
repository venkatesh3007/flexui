import Foundation

/// Handles fetching FlexUI configurations from remote servers
public final class ConfigFetcher {
    
    // MARK: - Properties
    
    private let baseURL: String
    private let connectTimeout: TimeInterval
    private let readTimeout: TimeInterval
    private let session: URLSession
    
    // MARK: - Initialization
    
    /// Initialize with configuration parameters
    public init(
        baseURL: String,
        connectTimeout: TimeInterval = 10,
        readTimeout: TimeInterval = 30
    ) {
        self.baseURL = baseURL
        self.connectTimeout = connectTimeout
        self.readTimeout = readTimeout
        
        // Configure URL session with timeouts
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = connectTimeout
        configuration.timeoutIntervalForResource = readTimeout
        self.session = URLSession(configuration: configuration)
    }
    
    // MARK: - Public Methods
    
    /// Fetch configuration synchronously (blocking)
    public func fetchConfigSync(screenId: String) -> Result<String, FlexError> {
        let semaphore = DispatchSemaphore(value: 0)
        var result: Result<String, FlexError>?
        
        fetchConfigAsync(screenId: screenId) { fetchResult in
            result = fetchResult
            semaphore.signal()
        }
        
        semaphore.wait()
        return result ?? .failure(.networkError("Request timed out"))
    }
    
    /// Fetch configuration asynchronously
    public func fetchConfigAsync(
        screenId: String,
        completion: @escaping (Result<String, FlexError>) -> Void
    ) {
        guard let url = buildURL(for: screenId) else {
            completion(.failure(.networkError("Invalid URL for screenId: \\(screenId)")))
            return
        }
        
        let request = buildRequest(for: url)
        
        session.dataTask(with: request) { [weak self] data, response, error in
            self?.handleResponse(data: data, response: response, error: error, completion: completion)
        }.resume()
    }
    
    // MARK: - Private Methods
    
    /// Build URL for screen configuration
    private func buildURL(for screenId: String) -> URL? {
        guard var components = URLComponents(string: baseURL) else {
            return nil
        }
        
        // Append path for screen configuration
        if components.path.isEmpty {
            components.path = "/screens/\\(screenId)"
        } else {
            components.path += "/screens/\\(screenId)"
        }
        
        // Add query parameters
        var queryItems = components.queryItems ?? []
        queryItems.append(URLQueryItem(name: "platform", value: "ios"))
        queryItems.append(URLQueryItem(name: "version", value: getAppVersion()))
        components.queryItems = queryItems
        
        return components.url
    }
    
    /// Build HTTP request with appropriate headers
    private func buildRequest(for url: URL) -> URLRequest {
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        request.setValue("FlexUI iOS/1.0", forHTTPHeaderField: "User-Agent")
        
        // Add device info headers
        request.setValue(UIDevice.current.systemVersion, forHTTPHeaderField: "X-iOS-Version")
        request.setValue(UIDevice.current.model, forHTTPHeaderField: "X-Device-Model")
        
        return request
    }
    
    /// Handle HTTP response and parse result
    private func handleResponse(
        data: Data?,
        response: URLResponse?,
        error: Error?,
        completion: @escaping (Result<String, FlexError>) -> Void
    ) {
        // Check for network error
        if let error = error {
            completion(.failure(.networkError("Network error: \\(error.localizedDescription)")))
            return
        }
        
        // Check for HTTP response
        guard let httpResponse = response as? HTTPURLResponse else {
            completion(.failure(.networkError("Invalid response type")))
            return
        }
        
        // Check status code
        guard 200...299 ~= httpResponse.statusCode else {
            let statusError = "HTTP error: \\(httpResponse.statusCode)"
            completion(.failure(.networkError(statusError)))
            return
        }
        
        // Check for data
        guard let data = data else {
            completion(.failure(.networkError("No data received")))
            return
        }
        
        // Validate data size
        if data.isEmpty {
            completion(.failure(.networkError("Empty response")))
            return
        }
        
        // Convert to string and validate JSON structure
        guard let jsonString = String(data: data, encoding: .utf8) else {
            completion(.failure(.parseError("Unable to decode response as UTF-8")))
            return
        }
        
        // Basic JSON validation
        if !isValidJSON(jsonString) {
            completion(.failure(.parseError("Invalid JSON format")))
            return
        }
        
        completion(.success(jsonString))
    }
    
    /// Validate that the string contains valid JSON
    private func isValidJSON(_ jsonString: String) -> Bool {
        guard let data = jsonString.data(using: .utf8) else {
            return false
        }
        
        do {
            _ = try JSONSerialization.jsonObject(with: data, options: [])
            return true
        } catch {
            return false
        }
    }
    
    /// Get current app version for API requests
    private func getAppVersion() -> String {
        return Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
    }
}

// MARK: - Result Extensions

public extension ConfigFetcher {
    
    /// Fetch configuration with retry logic
    func fetchConfigWithRetry(
        screenId: String,
        maxRetries: Int = 3,
        completion: @escaping (Result<String, FlexError>) -> Void
    ) {
        fetchConfigWithRetry(screenId: screenId, attempt: 1, maxRetries: maxRetries, completion: completion)
    }
    
    private func fetchConfigWithRetry(
        screenId: String,
        attempt: Int,
        maxRetries: Int,
        completion: @escaping (Result<String, FlexError>) -> Void
    ) {
        fetchConfigAsync(screenId: screenId) { result in
            switch result {
            case .success:
                completion(result)
            case .failure:
                if attempt < maxRetries {
                    // Exponential backoff: 1s, 2s, 4s...
                    let delay = pow(2.0, Double(attempt - 1))
                    DispatchQueue.global().asyncAfter(deadline: .now() + delay) {
                        self.fetchConfigWithRetry(
                            screenId: screenId,
                            attempt: attempt + 1,
                            maxRetries: maxRetries,
                            completion: completion
                        )
                    }
                } else {
                    completion(result)
                }
            }
        }
    }
}