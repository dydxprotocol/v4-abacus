//
//  Implementations.swift
//  dydxStateManager
//
//  Created by John Huang on 7/27/23.
//

import Abacus

extension IOImplementations {
    public static var shared: IOImplementations?

    public static func reset() {
        let rest = shared?.rest
        let webSocket = shared?.webSocket
        let fileSystem = shared?.fileSystem
        let threading = shared?.threading
        let tracking = shared?.tracking
        let chain = shared?.chain
        let timer = shared?.timer

          shared = IOImplementations(rest: rest ?? AbacusRestImp(), webSocket: webSocket ?? AbacusWebSocketImp(), chain: chain ?? AbacusChainImp(), tracking: tracking ?? AbacusTrackingImp(), threading: threading ?? AbacusThreadingImp(), timer: timer ?? AbacusTimerImp(), fileSystem: fileSystem ?? AbacusFileSystemImp())
    }
}

extension UIImplementations {
    public static var shared: UIImplementations?

    public static func reset(language: String?) {
        IOImplementations.reset()
        let systemLanguage = language ?? Locale.preferredLanguages.first
        #if DEBUG
            let loadLocalOnly = true
        #else
            let loadLocalOnly = false
        #endif
        let localizer = shared?.localizer ?? DynamicLocalizer(ioImplementations: IOImplementations.shared!,
                                                             systemLanguage: systemLanguage ?? "en",
                                                             path: "/config",
                                                             endpoint: "https://dydx-v4-shared-resources.vercel.app/config",
                                                             loadLocalOnly: loadLocalOnly)
        let formatter = shared?.formatter ?? AbacusFormatterImp()
        shared = UIImplementations(localizer: localizer, formatter: formatter)
    }
}
