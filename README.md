# javacard-rpc-server-javacard

On-card server runtime for [javacard-rpc](https://github.com/relux-works/javacard-rpc),
an RPC framework for Java Card smart-card applets. The javacard-rpc code generator
produces an applet skeleton on top of this runtime: request dispatch, argument
decoding, and response encoding over APDU are handled for you, so applet code stays
plain typed Java.

## Requirements

- Java Card SDK (applet target)
- Gradle (wrapper included: `./gradlew build`)

## Usage

Describe your interface in the javacard-rpc TOML IDL, generate the applet skeleton,
and implement the service methods. The
[javacard-rpc](https://github.com/relux-works/javacard-rpc) repository documents the
IDL, the generation workflow, and client runtimes for Swift, Kotlin/JVM, and Java.

<!-- relux-ecosystem:start -->

## About Relux Works

This project is part of the open-source ecosystem of
[Relux Works](https://relux.works), an AI-native software development studio.
We build fixed-price MVPs, rescue vibe-coded apps, run local AI inference, and
train teams to work with coding agents. Much of the infrastructure behind that
work is open source.

- Full catalog: [relux.works/en/open-source](https://relux.works/en/open-source/)
- Agentic enablement: [agent harnesses & team training](https://relux.works/en/agentic-enablement/)
- Hire us the agent-native way: point your assistant at `https://api.relux.works/mcp`
- Contact: ivan@relux.works

<!-- relux-ecosystem:end -->

## License

See [LICENSE](LICENSE).
