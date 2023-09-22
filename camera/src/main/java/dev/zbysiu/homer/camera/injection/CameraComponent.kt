package dev.zbysiu.homer.camera.injection

import dev.zbysiu.homer.camera.BarcodeCaptureActivity
import dev.zbysiu.homer.camera.BarcodeScanResultBottomSheetDialogFragment
import dev.zbysiu.homer.core.injection.CoreComponent
import dev.zbysiu.homer.core.injection.ModuleScope
import dagger.Component

@ModuleScope
@Component(dependencies = [CoreComponent::class])
interface CameraComponent {

    fun inject(fragment: BarcodeScanResultBottomSheetDialogFragment)

    fun inject(activity: BarcodeCaptureActivity)
}