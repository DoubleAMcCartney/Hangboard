#include <string.h>
 
#include "nrf_log.h"
#include "HAG_Service.h"
 
static const uint8_t DesiredCharName[] = "Desired";
static const uint8_t CurrentCharName[] = "Current";
static const uint8_t MoveCharName[] = "Move";
 
/**@brief Function for handling the Connect event.
 *
 * @param[in]   p_hag_service  HAG service structure.
 * @param[in]   p_ble_evt      Event received from the BLE stack.
 */
static void on_connect(ble_hag_service_t * p_hag_service, ble_evt_t const * p_ble_evt)
{
    p_hag_service->conn_handle = p_ble_evt->evt.gap_evt.conn_handle;
}
 
/**@brief Function for handling the Disconnect event.
 *
 * @param[in]   p_bas       HAG service structure.
 * @param[in]   p_ble_evt   Event received from the BLE stack.
 */
static void on_disconnect(ble_hag_service_t * p_hag_service, ble_evt_t const * p_ble_evt)
{
    UNUSED_PARAMETER(p_ble_evt);
    p_hag_service->conn_handle = BLE_CONN_HANDLE_INVALID;
}
 
/**@brief Function for handling the Write event.
 *
 * @param[in] p_hag_service   HAG Service structure.
 * @param[in] p_ble_evt       Event received from the BLE stack.
 */
static void on_write(ble_hag_service_t * p_hag_service, ble_evt_t const * p_ble_evt)
{
    ble_gatts_evt_write_t const * p_evt_write = &p_ble_evt->evt.gatts_evt.params.write;
 
    if (   (p_evt_write->handle == p_hag_service->desired_char_handles.value_handle)
        && (p_evt_write->len == 1)
        && (p_hag_service->hag_write_handler != NULL))
    {
        p_hag_service->hag_write_handler(p_ble_evt->evt.gap_evt.conn_handle, p_hag_service, p_evt_write->data[0]);
    }
}
 
/**@brief Function for adding the desired characteristic.
 *
 */
static uint32_t desired_char_add(ble_hag_service_t * p_hag_service)
{
    ble_gatts_char_md_t char_md;
    ble_gatts_attr_t    attr_char_value;
    ble_gatts_attr_md_t attr_md;
    ble_uuid_t          ble_uuid;
 
    memset(&char_md, 0, sizeof(char_md));
    memset(&attr_md, 0, sizeof(attr_md));
    memset(&attr_char_value, 0, sizeof(attr_char_value));
 
    char_md.char_props.read          = 1;
    char_md.char_props.write         = 1;
    char_md.p_char_user_desc         = DesiredCharName;
    char_md.char_user_desc_size      = sizeof(DesiredCharName);
    char_md.char_user_desc_max_size  = sizeof(DesiredCharName);
    char_md.p_char_pf                = NULL;
    char_md.p_user_desc_md           = NULL;
    char_md.p_cccd_md                = NULL;
    char_md.p_sccd_md                = NULL;
 
    // Define the Desired Characteristic UUID
    ble_uuid.type = p_hag_service->uuid_type;
    ble_uuid.uuid = BLE_UUID_DESIRED_CHAR_UUID;
 
    // Set permissions on the Characteristic value
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.write_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);
 
    // Attribute Metadata settings
    attr_md.vloc       = BLE_GATTS_VLOC_STACK;
    attr_md.rd_auth    = 0;
    attr_md.wr_auth    = 0;
    attr_md.vlen       = 0;
 
    // Attribute Value settings
    attr_char_value.p_uuid       = &ble_uuid;
    attr_char_value.p_attr_md    = &attr_md;
    attr_char_value.init_len     = sizeof(uint8_t);
    attr_char_value.init_offs    = 0;
    attr_char_value.max_len      = sizeof(uint8_t);
    attr_char_value.p_value      = NULL;
 
    return sd_ble_gatts_characteristic_add(p_hag_service->service_handle, &char_md,
                                           &attr_char_value,
                                           &p_hag_service->desired_char_handles);
}

/**@brief Function for adding the current characteristic.
 *
 */
static uint32_t current_char_add(ble_hag_service_t * p_hag_service)
{
    ble_gatts_char_md_t char_md;
    ble_gatts_attr_t    attr_char_value;
    ble_gatts_attr_md_t attr_md;
    ble_uuid_t          ble_uuid;
 
    memset(&char_md, 0, sizeof(char_md));
    memset(&attr_md, 0, sizeof(attr_md));
    memset(&attr_char_value, 0, sizeof(attr_char_value));
 
    char_md.char_props.read          = 1;
    char_md.char_props.write         = 1;
    char_md.p_char_user_desc         = CurrentCharName;
    char_md.char_user_desc_size      = sizeof(CurrentCharName);
    char_md.char_user_desc_max_size  = sizeof(CurrentCharName);
    char_md.p_char_pf                = NULL;
    char_md.p_user_desc_md           = NULL;
    char_md.p_cccd_md                = NULL;
    char_md.p_sccd_md                = NULL;
 
    // Define the Current Characteristic UUID
    ble_uuid.type = p_hag_service->uuid_type;
    ble_uuid.uuid = BLE_UUID_CURRENT_CHAR_UUID;
 
    // Set permissions on the Characteristic value
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.write_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);
 
    // Attribute Metadata settings
    attr_md.vloc       = BLE_GATTS_VLOC_STACK;
    attr_md.rd_auth    = 0;
    attr_md.wr_auth    = 0;
    attr_md.vlen       = 0;
 
    // Attribute Value settings
    attr_char_value.p_uuid       = &ble_uuid;
    attr_char_value.p_attr_md    = &attr_md;
    attr_char_value.init_len     = sizeof(uint8_t);
    attr_char_value.init_offs    = 0;
    attr_char_value.max_len      = sizeof(uint8_t);
    attr_char_value.p_value      = NULL;
 
    return sd_ble_gatts_characteristic_add(p_hag_service->service_handle, &char_md,
                                           &attr_char_value,
                                           &p_hag_service->current_char_handles);
}

/**@brief Function for adding the move characteristic.
 *
 */
static uint32_t move_char_add(ble_hag_service_t * p_hag_service)
{
    ble_gatts_char_md_t char_md;
    ble_gatts_attr_t    attr_char_value;
    ble_gatts_attr_md_t attr_md;
    ble_uuid_t          ble_uuid;
 
    memset(&char_md, 0, sizeof(char_md));
    memset(&attr_md, 0, sizeof(attr_md));
    memset(&attr_char_value, 0, sizeof(attr_char_value));
 
    char_md.char_props.read          = 1;
    char_md.char_props.write         = 1;
    char_md.p_char_user_desc         = MoveCharName;
    char_md.char_user_desc_size      = sizeof(MoveCharName);
    char_md.char_user_desc_max_size  = sizeof(MoveCharName);
    char_md.p_char_pf                = NULL;
    char_md.p_user_desc_md           = NULL;
    char_md.p_cccd_md                = NULL;
    char_md.p_sccd_md                = NULL;
 
    // Define the Move Characteristic UUID
    ble_uuid.type = p_hag_service->uuid_type;
    ble_uuid.uuid = BLE_UUID_MOVE_CHAR_UUID;
 
    // Set permissions on the Characteristic value
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.write_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);
 
    // Attribute Metadata settings
    attr_md.vloc       = BLE_GATTS_VLOC_STACK;
    attr_md.rd_auth    = 0;
    attr_md.wr_auth    = 0;
    attr_md.vlen       = 0;
 
    // Attribute Value settings
    attr_char_value.p_uuid       = &ble_uuid;
    attr_char_value.p_attr_md    = &attr_md;
    attr_char_value.init_len     = sizeof(uint8_t);
    attr_char_value.init_offs    = 0;
    attr_char_value.max_len      = sizeof(uint8_t);
    attr_char_value.p_value      = NULL;
 
    return sd_ble_gatts_characteristic_add(p_hag_service->service_handle, &char_md,
                                           &attr_char_value,
                                           &p_hag_service->desired_char_handles);
}
 
uint32_t ble_hag_service_init(ble_hag_service_t * p_hag_service, const ble_hag_service_init_t * p_hag_service_init)
{
    uint32_t   err_code;
    ble_uuid_t ble_uuid;
 
    // Initialize service structure
    p_hag_service->conn_handle = BLE_CONN_HANDLE_INVALID;
 
    // Initialize service structure.
    p_hag_service->hag_write_handler = p_hag_service_init->hag_write_handler;
 
    // Add service UUID
    ble_uuid128_t base_uuid = {BLE_UUID_HAG_SERVICE_BASE_UUID};
    err_code = sd_ble_uuid_vs_add(&base_uuid, &p_hag_service->uuid_type);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }
 
    // Set up the UUID for the service (base + service-specific)
    ble_uuid.type = p_hag_service->uuid_type;
    ble_uuid.uuid = BLE_UUID_HAG_SERVICE_UUID;
 
    // Set up and add the service
    err_code = sd_ble_gatts_service_add(BLE_GATTS_SRVC_TYPE_PRIMARY, &ble_uuid, &p_hag_service->service_handle);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }
 
    // Add the different characteristics in the service:
    //   Desired characteristic:   2D0319D8-DE15-11E8-9F32F-2801F1B9FD1
    err_code = desired_char_add(p_hag_service);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }

    //   Current characteristic:   4E0F43C2-DE15-11E8-9F32F-2801F1B9FD2
    err_code = current_char_add(p_hag_service);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }

    //   Move characteristic:   2d0317b2-DE15-11E8-9F32F-2801F1B9FD3
    err_code = move_char_add(p_hag_service);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }
 
    return NRF_SUCCESS;
}
 
void ble_hag_service_on_ble_evt(ble_evt_t const * p_ble_evt, void * p_context)
{
    ble_hag_service_t * p_hag_service = (ble_hag_service_t *)p_context;
 
    switch (p_ble_evt->header.evt_id)
    {
        case BLE_GAP_EVT_CONNECTED:
            on_connect(p_hag_service, p_ble_evt);
            break;
 
        case BLE_GATTS_EVT_WRITE:
            on_write(p_hag_service, p_ble_evt);
            break;
 
        case BLE_GAP_EVT_DISCONNECTED:
            on_disconnect(p_hag_service, p_ble_evt);
            break;
 
        default:
            // No implementation needed.
            break;
    }
}