Pod::Spec.new do |spec|
  spec.name          = "FlexUI"
  spec.version       = "1.0.0"
  spec.summary       = "Server-Driven Native UI SDK for iOS"
  spec.description   = <<-DESC
                       A lightweight library that renders native UI components from JSON configurations 
                       fetched from a server. Allows SDK developers to give their B2B customers UI 
                       customization without app updates.
                       DESC
  
  spec.homepage      = "https://github.com/flexui/flexui"
  spec.license       = { :type => "MIT", :file => "LICENSE" }
  spec.author        = { "FlexUI Team" => "hello@flexui.dev" }
  
  spec.platform      = :ios, "13.0"
  spec.swift_version = "5.7"
  
  spec.source        = { :git => "https://github.com/flexui/flexui.git", :tag => "#{spec.version}" }
  
  spec.source_files  = "Sources/FlexUI/**/*.{swift}"
  
  spec.framework     = "UIKit", "Foundation"
  
  spec.requires_arc  = true
  
  spec.dependency_specs = []
  
end