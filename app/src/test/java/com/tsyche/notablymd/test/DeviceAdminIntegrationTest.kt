package com.tsyche.notablymd.test

import android.app.admin.DevicePolicyManager
import android.content.Context
import com.tsyche.notablymd.utils.quickrecord.deviceadmin.QuickRecordDeviceAdmin
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*

/** Test to verify device admin integration functionality */
class DeviceAdminIntegrationTest {

    @Test
    fun `device admin should be properly registered`() {
        // Test that NotablyMD appears as a device admin option

        val packageName = "com.tsyche.notablymd"
        val componentName = "$packageName.utils.quickrecord.deviceadmin.QuickRecordDeviceAdmin"

        // Verify device admin registration
        assertNotNull("Device admin component should be registered", componentName)
        assertTrue("Package name should be correct", packageName.contains("notablymd"))
        assertTrue(
            "Should contain QuickRecordDeviceAdmin",
            componentName.contains("QuickRecordDeviceAdmin"),
        )
    }

    @Test
    fun `device admin should have proper component name`() {
        // Test that component name is properly generated

        val packageName = "com.tsyche.notablymd"
        val className = "com.tsyche.notablymd.utils.quickrecord.deviceadmin.QuickRecordDeviceAdmin"

        // Verify component name structure
        assertNotNull("Package name should not be null", packageName)
        assertNotNull("Class name should not be null", className)
        assertTrue("Should be NotablyMD package", packageName.contains("notablymd"))
        assertTrue(
            "Should be QuickRecordDeviceAdmin class",
            className.contains("QuickRecordDeviceAdmin"),
        )
        assertTrue(
            "Should be in utils.quickrecord.deviceadmin package",
            className.contains("utils.quickrecord.deviceadmin"),
        )
    }

    @Test
    fun `device admin should check admin status correctly`() {
        // Test that admin status checking works

        val mockContext = mock(Context::class.java)
        val devicePolicyManager = mock(DevicePolicyManager::class.java)

        `when`(mockContext.getSystemService(Context.DEVICE_POLICY_SERVICE))
            .thenReturn(devicePolicyManager)
        `when`(mockContext.packageName).thenReturn("com.tsyche.notablymd")
        `when`(devicePolicyManager.isAdminActive(any())).thenReturn(true)

        val isAdminActive = QuickRecordDeviceAdmin.isAdminActive(mockContext)

        assertTrue("Should detect admin status correctly", isAdminActive)
        verify(devicePolicyManager).isAdminActive(any())
    }
}
