package dev.zbysiu.homer.camera.injection

import android.content.Context
import dev.zbysiu.homer.core.injection.CoreInjectHelper

object CameraComponentProvider {

    private var cameraComponent: CameraComponent? = null

    fun get(context: Context): CameraComponent {

        if (cameraComponent == null) {
            cameraComponent = DaggerCameraComponent
                .builder()
                .coreComponent(CoreInjectHelper.provideCoreComponent(context))
                .build()
        }

        return cameraComponent!!
    }
}