import Foundation

/// Represents conditional logic for rendering FlexUI components
public struct FlexCondition: Codable, Equatable {
    
    // MARK: - Properties
    
    /// The condition expression or variable to evaluate
    public let condition: String
    
    /// The comparison operator (if comparing against a value)
    public let operator: String?
    
    /// The value to compare against
    public let value: AnyCodable?
    
    /// Logical operator for combining with other conditions
    public let logicalOperator: String?
    
    /// Additional conditions to combine with this one
    public let conditions: [FlexCondition]?
    
    // MARK: - Initialization
    
    public init(
        condition: String,
        operator: String? = nil,
        value: Any? = nil,
        logicalOperator: String? = nil,
        conditions: [FlexCondition]? = nil
    ) {
        self.condition = condition
        self.operator = `operator`
        self.value = value != nil ? AnyCodable(value!) : nil
        self.logicalOperator = logicalOperator
        self.conditions = conditions
    }
    
    // MARK: - Evaluation
    
    /// Evaluate this condition against the provided data and theme
    public func evaluate(data: [String: Any], theme: FlexTheme) -> Bool {
        // If there are multiple conditions, handle logical operators
        if let conditions = conditions, !conditions.isEmpty {
            return evaluateLogicalConditions(conditions, data: data, theme: theme)
        }
        
        // Evaluate single condition
        return evaluateSingleCondition(data: data, theme: theme)
    }
    
    // MARK: - Private Methods
    
    /// Evaluate a single condition
    private func evaluateSingleCondition(data: [String: Any], theme: FlexTheme) -> Bool {
        // Resolve the condition expression
        let resolvedCondition = resolveExpression(condition, data: data, theme: theme)
        
        // If there's no operator, treat as a boolean expression
        guard let op = operator, let compareValue = value else {
            return isTruthy(resolvedCondition)
        }
        
        // Resolve the comparison value
        let resolvedValue = resolveValue(compareValue.value, data: data, theme: theme)
        
        // Perform the comparison
        return performComparison(resolvedCondition, operator: op, value: resolvedValue)
    }
    
    /// Evaluate multiple conditions with logical operators
    private func evaluateLogicalConditions(_ conditions: [FlexCondition], data: [String: Any], theme: FlexTheme) -> Bool {
        guard !conditions.isEmpty else { return true }
        
        // Start with the first condition
        var result = conditions[0].evaluate(data: data, theme: theme)
        
        // Apply logical operators with remaining conditions
        for i in 1..<conditions.count {
            let condition = conditions[i]
            let conditionResult = condition.evaluate(data: data, theme: theme)
            
            // Use the logical operator from the previous condition
            let logicalOp = conditions[i-1].logicalOperator ?? "and"
            
            switch logicalOp.lowercased() {
            case "and", "&&":
                result = result && conditionResult
            case "or", "||":
                result = result || conditionResult
            case "xor":
                result = result != conditionResult
            default:
                result = result && conditionResult
            }
        }
        
        return result
    }
    
    /// Resolve an expression by replacing variables with actual values
    private func resolveExpression(_ expression: String, data: [String: Any], theme: FlexTheme) -> Any? {
        let resolver = ThemeResolver(theme: theme)
        let resolved = resolver.replaceVariables(expression, with: data)
        
        // Try to parse as different types
        if let boolValue = Bool(resolved) {
            return boolValue
        }
        
        if let intValue = Int(resolved) {
            return intValue
        }
        
        if let doubleValue = Double(resolved) {
            return doubleValue
        }
        
        // Try to resolve as a data path
        if resolved.contains(".") {
            return getNestedValue(from: data, path: resolved)
        }
        
        // Try direct data lookup
        if let dataValue = data[resolved] {
            return dataValue
        }
        
        // Return as string if nothing else works
        return resolved
    }
    
    /// Resolve a comparison value
    private func resolveValue(_ value: Any?, data: [String: Any], theme: FlexTheme) -> Any? {
        guard let value = value else { return nil }
        
        if let stringValue = value as? String {
            return resolveExpression(stringValue, data: data, theme: theme)
        }
        
        return value
    }
    
    /// Get nested value from data using dot notation (e.g., "user.profile.name")
    private func getNestedValue(from data: [String: Any], path: String) -> Any? {
        let keys = path.split(separator: ".").map(String.init)
        var current: Any? = data
        
        for key in keys {
            if let dict = current as? [String: Any] {
                current = dict[key]
            } else if let array = current as? [Any], let index = Int(key) {
                if index >= 0 && index < array.count {
                    current = array[index]
                } else {
                    return nil
                }
            } else {
                return nil
            }
        }
        
        return current
    }
    
    /// Check if a value is truthy
    private func isTruthy(_ value: Any?) -> Bool {
        guard let value = value else { return false }
        
        if let boolValue = value as? Bool {
            return boolValue
        }
        
        if let stringValue = value as? String {
            return !stringValue.isEmpty && stringValue.lowercased() != "false" && stringValue != "0"
        }
        
        if let intValue = value as? Int {
            return intValue != 0
        }
        
        if let doubleValue = value as? Double {
            return doubleValue != 0.0
        }
        
        if let arrayValue = value as? [Any] {
            return !arrayValue.isEmpty
        }
        
        if let dictValue = value as? [String: Any] {
            return !dictValue.isEmpty
        }
        
        return true
    }
    
    /// Perform comparison between two values
    private func performComparison(_ left: Any?, operator op: String, value right: Any?) -> Bool {
        switch op.lowercased() {
        case "==", "equals", "eq":
            return isEqual(left, right)
        case "!=", "not_equals", "ne":
            return !isEqual(left, right)
        case ">", "greater_than", "gt":
            return isGreaterThan(left, right)
        case ">=", "greater_than_or_equal", "gte":
            return isGreaterThanOrEqual(left, right)
        case "<", "less_than", "lt":
            return isLessThan(left, right)
        case "<=", "less_than_or_equal", "lte":
            return isLessThanOrEqual(left, right)
        case "contains":
            return contains(left, right)
        case "starts_with", "startswith":
            return startsWith(left, right)
        case "ends_with", "endswith":
            return endsWith(left, right)
        case "in":
            return isIn(left, right)
        case "regex", "matches":
            return matchesRegex(left, right)
        case "exists", "is_not_null":
            return left != nil
        case "not_exists", "is_null":
            return left == nil
        case "empty", "is_empty":
            return isEmpty(left)
        case "not_empty", "is_not_empty":
            return !isEmpty(left)
        default:
            return false
        }
    }
    
    // MARK: - Comparison Helper Methods
    
    private func isEqual(_ left: Any?, _ right: Any?) -> Bool {
        guard let left = left, let right = right else {
            return left == nil && right == nil
        }
        
        // String comparison
        if let leftString = left as? String, let rightString = right as? String {
            return leftString == rightString
        }
        
        // Numeric comparison
        if let leftNumber = asNumber(left), let rightNumber = asNumber(right) {
            return leftNumber == rightNumber
        }
        
        // Boolean comparison
        if let leftBool = left as? Bool, let rightBool = right as? Bool {
            return leftBool == rightBool
        }
        
        return false
    }
    
    private func isGreaterThan(_ left: Any?, _ right: Any?) -> Bool {
        guard let leftNumber = asNumber(left), let rightNumber = asNumber(right) else {
            return false
        }
        return leftNumber > rightNumber
    }
    
    private func isGreaterThanOrEqual(_ left: Any?, _ right: Any?) -> Bool {
        guard let leftNumber = asNumber(left), let rightNumber = asNumber(right) else {
            return false
        }
        return leftNumber >= rightNumber
    }
    
    private func isLessThan(_ left: Any?, _ right: Any?) -> Bool {
        guard let leftNumber = asNumber(left), let rightNumber = asNumber(right) else {
            return false
        }
        return leftNumber < rightNumber
    }
    
    private func isLessThanOrEqual(_ left: Any?, _ right: Any?) -> Bool {
        guard let leftNumber = asNumber(left), let rightNumber = asNumber(right) else {
            return false
        }
        return leftNumber <= rightNumber
    }
    
    private func contains(_ left: Any?, _ right: Any?) -> Bool {
        if let leftString = left as? String, let rightString = right as? String {
            return leftString.contains(rightString)
        }
        
        if let leftArray = left as? [Any] {
            return leftArray.contains { element in
                isEqual(element, right)
            }
        }
        
        return false
    }
    
    private func startsWith(_ left: Any?, _ right: Any?) -> Bool {
        guard let leftString = left as? String, let rightString = right as? String else {
            return false
        }
        return leftString.hasPrefix(rightString)
    }
    
    private func endsWith(_ left: Any?, _ right: Any?) -> Bool {
        guard let leftString = left as? String, let rightString = right as? String else {
            return false
        }
        return leftString.hasSuffix(rightString)
    }
    
    private func isIn(_ left: Any?, _ right: Any?) -> Bool {
        guard let rightArray = right as? [Any] else { return false }
        
        return rightArray.contains { element in
            isEqual(left, element)
        }
    }
    
    private func matchesRegex(_ left: Any?, _ right: Any?) -> Bool {
        guard let leftString = left as? String,
              let rightPattern = right as? String else {
            return false
        }
        
        do {
            let regex = try NSRegularExpression(pattern: rightPattern)
            let range = NSRange(leftString.startIndex..<leftString.endIndex, in: leftString)
            return regex.firstMatch(in: leftString, range: range) != nil
        } catch {
            return false
        }
    }
    
    private func isEmpty(_ value: Any?) -> Bool {
        guard let value = value else { return true }
        
        if let stringValue = value as? String {
            return stringValue.isEmpty
        }
        
        if let arrayValue = value as? [Any] {
            return arrayValue.isEmpty
        }
        
        if let dictValue = value as? [String: Any] {
            return dictValue.isEmpty
        }
        
        return false
    }
    
    private func asNumber(_ value: Any?) -> Double? {
        guard let value = value else { return nil }
        
        if let doubleValue = value as? Double {
            return doubleValue
        }
        
        if let floatValue = value as? Float {
            return Double(floatValue)
        }
        
        if let intValue = value as? Int {
            return Double(intValue)
        }
        
        if let stringValue = value as? String {
            return Double(stringValue)
        }
        
        return nil
    }
}

// MARK: - Factory Methods

public extension FlexCondition {
    
    /// Create a simple boolean condition
    static func boolean(_ expression: String) -> FlexCondition {
        return FlexCondition(condition: expression)
    }
    
    /// Create an equality condition
    static func equals(_ expression: String, value: Any) -> FlexCondition {
        return FlexCondition(condition: expression, operator: "==", value: value)
    }
    
    /// Create a greater than condition
    static func greaterThan(_ expression: String, value: Any) -> FlexCondition {
        return FlexCondition(condition: expression, operator: ">", value: value)
    }
    
    /// Create a contains condition
    static func contains(_ expression: String, value: Any) -> FlexCondition {
        return FlexCondition(condition: expression, operator: "contains", value: value)
    }
    
    /// Create an exists condition
    static func exists(_ expression: String) -> FlexCondition {
        return FlexCondition(condition: expression, operator: "exists")
    }
    
    /// Create an AND condition combining multiple conditions
    static func and(_ conditions: [FlexCondition]) -> FlexCondition {
        guard !conditions.isEmpty else {
            return FlexCondition(condition: "true")
        }
        
        let first = conditions[0]
        let rest = Array(conditions.dropFirst())
        
        return FlexCondition(
            condition: first.condition,
            operator: first.operator,
            value: first.value?.value,
            logicalOperator: "and",
            conditions: rest
        )
    }
    
    /// Create an OR condition combining multiple conditions
    static func or(_ conditions: [FlexCondition]) -> FlexCondition {
        guard !conditions.isEmpty else {
            return FlexCondition(condition: "false")
        }
        
        let first = conditions[0]
        let rest = Array(conditions.dropFirst())
        
        return FlexCondition(
            condition: first.condition,
            operator: first.operator,
            value: first.value?.value,
            logicalOperator: "or",
            conditions: rest
        )
    }
}

// MARK: - Codable Keys

extension FlexCondition {
    private enum CodingKeys: String, CodingKey {
        case condition = "if"
        case `operator` = "operator"
        case value
        case logicalOperator
        case conditions
    }
}