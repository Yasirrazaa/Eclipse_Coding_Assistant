# Groq Assistant Eclipse Plugin

An intelligent Eclipse plugin that enhances Java development workflows using the power of Groq's AI. This plugin provides AI-powered code generation, completion, analysis, and refactoring capabilities to boost developer productivity.

## Features

### 1. Code Generation
Generate high-quality Java code from natural language descriptions:
- **Natural Language Input**: Describe what you want to create in plain English
- **Syntax Highlighting**: Generated code is properly formatted and highlighted
- **Background Processing**: Non-blocking operation for better user experience
- **Error Handling**: Robust error handling with clear feedback
- **Access**: Via menu "Groq Assistant > Generate Code" or right-click "Generate Code with Groq"

### 2. Code Completion
Intelligent, context-aware code suggestions:
- **Real-time Suggestions**: AI-powered completions as you type
- **Context Awareness**: Understands your code context for better suggestions
- **Fast Response**: 2-second timeout for quick feedback
- **Fallback Mechanism**: Graceful fallback to basic completions if needed
- **Integration**: Seamlessly integrated with Eclipse's completion system

### 3. Code Analysis
AI-powered code analysis for better quality:
- **Intelligent Analysis**: Deep understanding of code patterns and best practices
- **Multiple Severity Levels**: ERROR, WARNING, INFO, and SUGGESTION
- **Quick Fixes**: Suggested improvements with explanations
- **Visual Feedback**: Problems view integration and in-editor markers
- **Access**: Via menu "Groq Assistant > Analyze Current File" or right-click "Analyze with Groq"

### 4. Code Refactoring
Smart refactoring suggestions to improve code quality:
- **Multiple Patterns**:
  - Extract Method
  - Extract Class
  - Rename
  - Inline
  - Encapsulate Field
  - General Improvement
- **Selection Support**: Refactor selected code or entire file
- **Preview Changes**: Review changes before applying
- **Explanation**: Clear explanation of refactoring benefits
- **Access**: Via menu "Groq Assistant > Refactor Code" or right-click "Refactor with Groq"

## Installation

1. Prerequisites:
   - Eclipse IDE (2021-03 or later)
   - Java Development Kit (JDK) 11 or later
   - Active Groq API key

2. Install from Update Site:
   - Help > Install New Software
   - Add the plugin update site
   - Select "Groq Assistant"
   - Follow the installation wizard
   - Restart Eclipse when prompted

3. Configure:
   - Window > Preferences > Groq Assistant
   - Enter your Groq API key
   - Adjust any other preferences as needed

## Usage

### Code Generation
1. Open the Code Generation view:
   - Window > Show View > Groq Assistant > Code Generation
2. Enter a description of the code you want to generate
3. Click "Generate Code"
4. Review and use the generated code

### Code Completion
1. Start typing in a Java editor
2. Press Ctrl+Space to trigger completion
3. AI-powered suggestions will appear in the completion list
4. Press Enter to accept a suggestion

### Code Analysis
1. Open a Java file
2. Choose one:
   - Click "Groq Assistant > Analyze Current File"
   - Right-click > Analyze with Groq
3. Review analysis results in:
   - Problems view
   - Editor markers
   - Quick-fix suggestions

### Code Refactoring
1. Open a Java file
2. Optionally select code to refactor
3. Choose one:
   - Click "Groq Assistant > Refactor Code"
   - Right-click > Refactor with Groq
4. Select refactoring type
5. Review and apply changes

## Best Practices

1. API Key Security:
   - Store API key in Eclipse preferences
   - Never commit API key to version control
   - Use environment variables in CI/CD

2. Performance:
   - Use code completion judiciously
   - Run analysis on focused sections of code
   - Consider network connectivity

3. Code Quality:
   - Review generated code before use
   - Test refactored code thoroughly
   - Use analysis suggestions as guidelines

## Troubleshooting

Common issues and solutions:

1. API Key Issues:
   - Verify key in preferences
   - Check network connectivity
   - Ensure key has proper permissions

2. Performance Issues:
   - Check network speed
   - Reduce scope of analysis
   - Update plugin to latest version

3. Integration Issues:
   - Verify Eclipse version compatibility
   - Check Java project configuration
   - Review error logs

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Groq AI for providing the powerful AI capabilities
- Eclipse Foundation for the robust plugin development framework
- The open-source community for various helpful libraries

## Support

For issues, questions, or suggestions:
- Open an issue in the GitHub repository
- Contact the development team
- Check the documentation

## Future Plans

Upcoming features and improvements:
1. Additional programming language support
2. More refactoring patterns
3. Enhanced analysis capabilities
4. Performance optimizations
5. Expanded configuration options
