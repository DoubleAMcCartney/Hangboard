#ifndef HAG_SERVICE_H
#define HAG_SERVICE_H
 
#include <string.h>
#include "ble.h"
#include "ble_srv_common.h"
 
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
                     ble_hag_on_ble_evt, &_name)
 
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
#define BLE_UUID_HAG_SERVICE_UUID   0x3a3a
#define BLE_UUID_DESIRED_CHAR_UUID  0x9fd1
#define BLE_UUID_CURRENT_CHAR_UUID  0x9fd2
#define BLE_UUID_MOVE_CHAR_UUID     0x9fd3
 
/**@brief HAG Service event type. */
typedef enum
{
    BLE_HAG_EVT_NOTIFICATION_ENABLED,                             /**< HAG value notification enabled event. */
    BLE_HAG_EVT_NOTIFICATION_DISABLED,                             /**< HAG value notification disabled event. */
    BLE_HAG_EVT_DISCONNECTED,
    BLE_HAG_EVT_CONNECTED,
    BLE_HAG_EVT_DESIRED_UPDATED
} ble_hag_evt_type_t;

/**@brief HAG Service event. */
typedef struct
{
    ble_hag_evt_type_t evt_type;                                  /**< Type of event. */
    uint8_t desired_angle;
    uint8_t desired_depth;
} ble_hag_evt_t;

// Forward declaration of the ble_hag_service_t type.
typedef struct ble_hag_service_s ble_hag_service_t;


/**@brief HAG Service event handler type. */
typedef void (*ble_hag_evt_handler_t) (ble_hag_service_t * p_bas, ble_hag_evt_t * p_evt);

/**@brief HAG Service init structure. This contains all options and data needed for
 *        initialization of the service.*/
typedef struct
{
    ble_hag_evt_handler_t         evt_handler;                    /**< Event handler to be called for handling events in the HAG Service. */
    uint8_t                       initial_desired_value;          /**< Initial desired value */
    uint8_t                       initial_current_value;          /**< Initial current value */
    uint8_t                       initial_move_value;             /**< Initial move value */
    ble_srv_cccd_security_mode_t  desired_value_char_attr_md;     /**< Initial security level for Desired characteristics attribute */
    ble_srv_cccd_security_mode_t  current_value_char_attr_md;     /**< Initial security level for Current characteristics attribute */
    ble_srv_cccd_security_mode_t  move_value_char_attr_md;        /**< Initial security level for Move characteristics attribute */
} ble_hag_init_t;

/**@brief HAG Service structure. This contains various status information for the service. */
struct ble_hag_service_s
{
    ble_hag_evt_handler_t         evt_handler;                    /**< Event handler to be called for handling events in the HAG Service. */
    uint16_t                      service_handle;                 /**< Handle of HAG Service (as provided by the BLE stack). */
    ble_gatts_char_handles_t      desired_value_handles;          /**< Handles related to the Desired Value characteristic. */
    ble_gatts_char_handles_t      current_value_handles;          /**< Handles related to the Current Value characteristic. */
    ble_gatts_char_handles_t      move_value_handles;             /**< Handles related to the Move Value characteristic. */
    uint16_t                      conn_handle;                    /**< Handle of the current connection (as provided by the BLE stack, is BLE_CONN_HANDLE_INVALID if not in a connection). */
    uint8_t                       uuid_type; 
};

/**@brief Function for initializing the HAG Service.
 *
 * @param[out]  p_hag       HAG Service structure. This structure will have to be supplied by
 *                          the application. It will be initialized by this function, and will later
 *                          be used to identify this particular service instance.
 * @param[in]   p_hag_init  Information needed to initialize the service.
 *
 * @return      NRF_SUCCESS on successful initialization of service, otherwise an error code.
 */
uint32_t ble_hag_init(ble_hag_service_t * p_hag, const ble_hag_init_t * p_hag_init);

/**@brief Function for handling the Application's BLE Stack events.
 *
 * @details Handles all events from the BLE stack of interest to the HAG Service.
 *
 * @note 
 *
 * @param[in]   p_hag      HAG Service structure.
 * @param[in]   p_ble_evt  Event received from the BLE stack.
 */
void ble_hag_on_ble_evt( ble_evt_t const * p_ble_evt, void * p_context);

/**@brief Function for updating the current value.
 *
 * @details The application calls this function when the current value should be updated. If
 *          notification has been enabled, the current value characteristic is sent to the client.
 *
 * @note 
 *       
 * @param[in]   p_bas          HAG Service structure.
 * @param[in]   HAG value 
 *
 * @return      NRF_SUCCESS on success, otherwise an error code.
 */

uint32_t ble_hag_current_value_update(ble_hag_service_t * p_hag, uint8_t current_value[]);

/**@brief Function for updating the move value.
 *
 * @details The application calls this function when the move value should be updated. If
 *          notification has been enabled, the current value characteristic is sent to the client.
 *
 * @note 
 *       
 * @param[in]   p_bas          HAG Service structure.
 * @param[in]   HAG value 
 *
 * @return      NRF_SUCCESS on success, otherwise an error code.
 */

uint32_t ble_hag_move_value_update(ble_hag_service_t * p_hag, uint8_t move_value);

#endif /* HAG_SERVICE_H */