package io.jadu.ringlr

import androidx.compose.ui.window.ComposeUIViewController
import io.jadu.ringlr.call.PlatformConfiguration
import platform.UIKit.UIViewController

/**
 * Creates the root UIViewController for the iOS host app.
 *
 * Call this from your Swift AppDelegate or SwiftUI entry point:
 *
 * ```swift
 * import demoApp
 *
 * struct ContentView: View {
 *     var body: some View {
 *         MainViewControllerRepresentable()
 *     }
 * }
 *
 * struct MainViewControllerRepresentable: UIViewControllerRepresentable {
 *     func makeUIViewController(context: Context) -> UIViewController {
 *         MainViewControllerKt.MainViewController()
 *     }
 *     func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
 * }
 * ```
 */
fun MainViewController(): UIViewController {
    val configuration = PlatformConfiguration.create()
    return ComposeUIViewController {
        App(configuration = configuration)
    }
}
