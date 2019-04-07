#ifndef HAG_SERVICE_H
#define HAG_SERVICE_H
 
#include <string.h>
#include "boards.h"
#include "ble.h"
#include "ble_srv_common.h"
#include "nrf_sdh_ble.h"
 
/**@brief   Macro for defining a ble_hag_service instance.
 *
 * @param   _name   Name of the instance.
 * @hideinitializer
 */
 
#define BLE_HAG_SERVICE_BLE_OBSERVER_PRIO 2
 
#define BLE_HAG_SERVICE_DEF(_name)                                                                          \
static ble_hag_service_t _name;                                                                             \
NRF_SDH_BLE_OBSERVER(_name ## _obs,                                                                         \
                     BLE_HAG_SERVICE_BLE_OBSERVER_PRIO,                                                     \
                     ble_hag_service_on_ble_evt, &_name)
 
//    HAG service:              53E74E5A-D192-47A0-8F06-35EC90C73A3A
//    Desired characteristic:   2D0319D8-DE15-11E8-9F32F-2801F1B9FD1
//    Current characteristic:   4E0F43C2-DE15-11E8-9F32F-2801F1B9FD2
//    Move characteristic:      2d0317b2-DE15-11E8-9F32F-2801F1B9FD3
 
// The bytes are stored in little-endian format, meaning the
// Least Significant Byte is stored first
// (reversed from the order they're displayed as)
 
// Base UUID: 53E74E5A-D192-47A0-8F06-35EC90C73A3A
#define BLE_UUID_HAG_SERVICE_BASE_UUID  {0x3A, 0x3A, 0xC7, 0x90, 0xEC, 0x35, 0x06, 0x8F, 0xA0, 0x47, 0x92, 0xD1, 0x5A, 0x4E, 0xE7, 0x53}
 
// Service &amp; characteristics UUIDs
#define BLE_UUID_HAG_SERVICE_UUID   0x0001
#define BLE_UUID_DESIRED_CHAR_UUID  0x0002
#define BLE_UUID_CURRENT_CHAR_UUID  0x0003
#define BLE_UUID_MOVE_CHAR_UUID     0x0004
 
// Forward declaration of the custom_service_t type.
typedef struct ble_hag_service_s ble_hag_service_t;
 
typedef void (*ble_hag_service_hag_write_handler_t) (uint16_t conn_handle, ble_hag_service_t * p_hag_service, uint8_t new_state);
 
/** @brief HAG Service init structure. This structure contains all options and data needed for
 *        initialization of the service.*/
typedef struct
{
    ble_hag_service_hag_write_handler_t hag_write_handler; /**&lt; Event handler to be called when the HAG Characteristic is written. */
} ble_hag_service_init_t;
 
/**@brief HAG Service structure.
 *        This contains various status information
 *        for the service.
 */
typedef struct ble_hag_service_s
{
    uint16_t                            conn_handle;
    uint16_t                            service_handle;
    uint8_t                             uuid_type;
    ble_gatts_char_handles_t            desired_char_handles;
    ble_gatts_char_handles_t            current_char_handles;
    ble_gatts_char_handles_t            move_char_handles;
    ble_hag_service_hag_write_handler_t hag_write_handler;
 
} ble_hag_service_t;
 
// Function Declarations
 
/**@brief Function for initializing the HAG Service.
 *
 * @param[out]  p_hag_service  HAG Service structure. This structure will have to be supplied by
 *                                the application. It will be initialized by this function, and will later
 *                                be used to identify this particular service instance.
 *
 * @return      NRF_SUCCESS on successful initialization of service, otherwise an error code.
 */
uint32_t ble_hag_service_init(ble_hag_service_t * p_hag_service, const ble_hag_service_init_t * p_hag_service_init);
 
/**@brief Function for handling the application's BLE stack events.
 *
 * @details This function handles all events from the BLE stack that are of interest to the HAG Service.
 *
 * @param[in] p_ble_evt  Event received from the BLE stack.
 * @param[in] p_context  HAG Service structure.
 */
void ble_hag_service_on_ble_evt(ble_evt_t const * p_ble_evt, void * p_context);
 
#endif /* HAG_SERVICE_H */