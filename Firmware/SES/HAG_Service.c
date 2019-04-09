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

    ble_hag_evt_t evt;
    evt.evt_type = BLE_HAG_EVT_CONNECTED;
    p_hag_service->evt_handler(p_hag_service, &evt);
}
 
/**@brief Function for handling the Disconnect event.
 *
 * @param[in]   p_hag_service       HAG service structure.
 * @param[in]   p_ble_evt   Event received from the BLE stack.
 */
static void on_disconnect(ble_hag_service_t * p_hag_service, ble_evt_t const * p_ble_evt)
{
    UNUSED_PARAMETER(p_ble_evt);
    p_hag_service->conn_handle = BLE_CONN_HANDLE_INVALID;
    
    ble_hag_evt_t evt;
    evt.evt_type = BLE_HAG_EVT_DISCONNECTED;
    p_hag_service->evt_handler(p_hag_service, &evt);
}
 
/**@brief Function for handling the Write event.
 *
 * @param[in] p_hag_service   HAG Service structure.
 * @param[in] p_ble_evt       Event received from the BLE stack.
 */
static void on_write(ble_hag_service_t * p_hag_service, ble_evt_t const * p_ble_evt)
{
    ble_gatts_evt_write_t const * p_evt_write = &p_ble_evt->evt.gatts_evt.params.write;
 
    if (   (p_evt_write->handle == p_hag_service->desired_value_handles.cccd_handle)
        && (p_evt_write->len == 2)
        && (p_hag_service->evt_handler != NULL))
    {
        ble_hag_evt_t evt;

        if (ble_srv_is_notification_enabled(p_evt_write->data))
        {
            evt.evt_type = BLE_HAG_EVT_NOTIFICATION_ENABLED;
        }
        else
        {
            evt.evt_type = BLE_HAG_EVT_NOTIFICATION_DISABLED;
        }
        // Call the application event handler.
        p_hag_service->evt_handler(p_hag_service, &evt);
    }
}

void ble_hag_on_ble_evt( ble_evt_t const * p_ble_evt, void * p_context)
{
    ble_hag_service_t * p_hag = (ble_hag_service_t *) p_context;
    
    NRF_LOG_INFO("BLE event received. Event type = %d\r\n", p_ble_evt->header.evt_id); 
    if (p_hag == NULL || p_ble_evt == NULL)
    {
        return;
    }
    
    switch (p_ble_evt->header.evt_id)
    {
        case BLE_GAP_EVT_CONNECTED:
            on_connect(p_hag, p_ble_evt);
            break;

        case BLE_GAP_EVT_DISCONNECTED:
            on_disconnect(p_hag, p_ble_evt);
            break;

        case BLE_GATTS_EVT_WRITE:
            on_write(p_hag, p_ble_evt);
            break;

        default:
            // No implementation needed.
            break;
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
    char_md.char_props.notify        = 0;
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
    attr_char_value.init_len     = 2;
    attr_char_value.init_offs    = 0;
    attr_char_value.max_len      = 2;
    attr_char_value.p_value      = NULL;
 
    return sd_ble_gatts_characteristic_add(p_hag_service->service_handle, &char_md,
                                           &attr_char_value,
                                           &p_hag_service->desired_value_handles);
}

/**@brief Function for adding the current characteristic.
 *
 */
static uint32_t current_char_add(ble_hag_service_t * p_hag_service)
{
    ble_gatts_char_md_t char_md;
    ble_gatts_attr_md_t cccd_md;
    ble_gatts_attr_t    attr_char_value;
    ble_gatts_attr_md_t attr_md;
    ble_uuid_t          ble_uuid;
 
    memset(&char_md, 0, sizeof(char_md));
    memset(&attr_md, 0, sizeof(attr_md));
    memset(&cccd_md, 0, sizeof(cccd_md));
    memset(&attr_char_value, 0, sizeof(attr_char_value));
 
    char_md.char_props.read          = 1;
    char_md.char_props.write         = 1;
    char_md.char_props.notify        = 1;
    char_md.p_char_user_desc         = CurrentCharName;
    char_md.char_user_desc_size      = sizeof(CurrentCharName);
    char_md.char_user_desc_max_size  = sizeof(CurrentCharName);
    char_md.p_char_pf                = NULL;
    char_md.p_user_desc_md           = NULL;
    char_md.p_cccd_md                = &cccd_md;
    char_md.p_sccd_md                = NULL;
 
    // Define the Current Characteristic UUID
    ble_uuid.type = p_hag_service->uuid_type;
    ble_uuid.uuid = BLE_UUID_CURRENT_CHAR_UUID;
 
    // Set permissions on the Characteristic value
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);

    //  Read  operation on Cccd should be possible without authentication.
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&cccd_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&cccd_md.write_perm);
    cccd_md.vloc       = BLE_GATTS_VLOC_STACK;
 
    // Attribute Metadata settings
    attr_md.vloc       = BLE_GATTS_VLOC_STACK;
    attr_md.rd_auth    = 0;
    attr_md.wr_auth    = 0;
    attr_md.vlen       = 0;
 
    // Attribute Value settings
    attr_char_value.p_uuid       = &ble_uuid;
    attr_char_value.p_attr_md    = &attr_md;
    attr_char_value.init_len     = 4;
    attr_char_value.init_offs    = 0;
    attr_char_value.max_len      = 4;
    attr_char_value.p_value      = NULL;
 
    return sd_ble_gatts_characteristic_add(p_hag_service->service_handle, &char_md,
                                           &attr_char_value,
                                           &p_hag_service->current_value_handles);
}

/**@brief Function for adding the move characteristic.
 *
 */
static uint32_t move_char_add(ble_hag_service_t * p_hag_service)
{
    ble_gatts_char_md_t char_md;
    ble_gatts_attr_md_t cccd_md;
    ble_gatts_attr_t    attr_char_value;
    ble_gatts_attr_md_t attr_md;
    ble_uuid_t          ble_uuid;
 
    memset(&char_md, 0, sizeof(char_md));
    memset(&attr_md, 0, sizeof(attr_md));
    memset(&cccd_md, 0, sizeof(cccd_md));
    memset(&attr_char_value, 0, sizeof(attr_char_value));
 
    char_md.char_props.read          = 1;
    char_md.char_props.write         = 1;
    char_md.char_props.notify        = 1;
    char_md.p_char_user_desc         = MoveCharName;
    char_md.char_user_desc_size      = sizeof(MoveCharName);
    char_md.char_user_desc_max_size  = sizeof(MoveCharName);
    char_md.p_char_pf                = NULL;
    char_md.p_user_desc_md           = NULL;
    char_md.p_cccd_md                = &cccd_md;
    char_md.p_sccd_md                = NULL;
 
    // Define the Move Characteristic UUID
    ble_uuid.type = p_hag_service->uuid_type;
    ble_uuid.uuid = BLE_UUID_MOVE_CHAR_UUID;
 
    // Set permissions on the Characteristic value
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);

    //  Read  operation on Cccd should be possible without authentication.
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&cccd_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&cccd_md.write_perm);
    cccd_md.vloc       = BLE_GATTS_VLOC_STACK;
 
    // Attribute Metadata settings
    attr_md.vloc       = BLE_GATTS_VLOC_STACK;
    attr_md.rd_auth    = 0;
    attr_md.wr_auth    = 0;
    attr_md.vlen       = 0;
 
    // Attribute Value settings
    attr_char_value.p_uuid       = &ble_uuid;
    attr_char_value.p_attr_md    = &attr_md;
    attr_char_value.init_len     = sizeof(uint8_t); // 1 byte
    attr_char_value.init_offs    = 0;
    attr_char_value.max_len      = sizeof(uint8_t);
    attr_char_value.p_value      = NULL;
 
    return sd_ble_gatts_characteristic_add(p_hag_service->service_handle, &char_md,
                                           &attr_char_value,
                                           &p_hag_service->desired_value_handles);
}
 

uint32_t ble_hag_init(ble_hag_service_t * p_hag, const ble_hag_init_t * p_hag_init)
{
    if (p_hag == NULL || p_hag_init == NULL)
    {
        return NRF_ERROR_NULL;
    }

    uint32_t   err_code;
    ble_uuid_t ble_uuid;

    // Initialize service structure
    p_hag->evt_handler               = p_hag_init->evt_handler;
    p_hag->conn_handle               = BLE_CONN_HANDLE_INVALID;

    // Add HAG Service UUID
    ble_uuid128_t base_uuid = {BLE_UUID_HAG_SERVICE_BASE_UUID};
    err_code =  sd_ble_uuid_vs_add(&base_uuid, &p_hag->uuid_type);
    VERIFY_SUCCESS(err_code);
    
    ble_uuid.type = p_hag->uuid_type;
    ble_uuid.uuid = BLE_UUID_HAG_SERVICE_UUID;

    // Add the HAG Service
    err_code = sd_ble_gatts_service_add(BLE_GATTS_SRVC_TYPE_PRIMARY, &ble_uuid, &p_hag->service_handle);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }

    // Add the different characteristics in the service:
    //   Desired characteristic:   2D0319D8-DE15-11E8-9F32F-2801F1B9FD1
    err_code = desired_char_add(p_hag);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }

    //   Current characteristic:   4E0F43C2-DE15-11E8-9F32F-2801F1B9FD2
    err_code = current_char_add(p_hag);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }

    //   Move characteristic:   2d0317b2-DE15-11E8-9F32F-2801F1B9FD3
    err_code = move_char_add(p_hag);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }
 
    return NRF_SUCCESS;
}

uint32_t ble_hag_current_value_update(ble_hag_service_t * p_hag, uint8_t current_value[])
{
    NRF_LOG_INFO("In ble_hag_current_value_update. \r\n"); 
    if (p_hag == NULL)
    {
        return NRF_ERROR_NULL;
    }

    uint32_t err_code = NRF_SUCCESS;
    ble_gatts_value_t gatts_value;

    // Initialize value struct.
    memset(&gatts_value, 0, sizeof(gatts_value));

    gatts_value.len     = 4;
    gatts_value.offset  = 0;
    gatts_value.p_value = current_value;

    // Update database.
    err_code = sd_ble_gatts_value_set(p_hag->conn_handle,
                                      p_hag->current_value_handles.value_handle,
                                      &gatts_value);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }

    // Send value if connected and notifying.
    if ((p_hag->conn_handle != BLE_CONN_HANDLE_INVALID)) 
    {
        ble_gatts_hvx_params_t hvx_params;

        memset(&hvx_params, 0, sizeof(hvx_params));

        hvx_params.handle = p_hag->current_value_handles.value_handle;
        hvx_params.type   = BLE_GATT_HVX_NOTIFICATION;
        hvx_params.offset = gatts_value.offset;
        hvx_params.p_len  = &gatts_value.len;
        hvx_params.p_data = gatts_value.p_value;

        err_code = sd_ble_gatts_hvx(p_hag->conn_handle, &hvx_params);
        NRF_LOG_INFO("sd_ble_gatts_hvx result: %x. \r\n", err_code); 
    }
    else
    {
        err_code = NRF_ERROR_INVALID_STATE;
        NRF_LOG_INFO("sd_ble_gatts_hvx result: NRF_ERROR_INVALID_STATE. \r\n"); 
    }


    return err_code;
}

uint32_t ble_hag_move_value_update(ble_hag_service_t * p_hag, uint8_t move_value)
{
    NRF_LOG_INFO("In ble_hag_move_value_update. \r\n"); 
    if (p_hag == NULL)
    {
        return NRF_ERROR_NULL;
    }

    uint32_t err_code = NRF_SUCCESS;
    ble_gatts_value_t gatts_value;

    // Initialize value struct.
    memset(&gatts_value, 0, sizeof(gatts_value));

    gatts_value.len     = sizeof(uint8_t);
    gatts_value.offset  = 0;
    gatts_value.p_value = &move_value;

    // Update database.
    err_code = sd_ble_gatts_value_set(p_hag->conn_handle,
                                      p_hag->move_value_handles.value_handle,
                                      &gatts_value);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }

    // Send value if connected and notifying.
    if ((p_hag->conn_handle != BLE_CONN_HANDLE_INVALID)) 
    {
        ble_gatts_hvx_params_t hvx_params;

        memset(&hvx_params, 0, sizeof(hvx_params));

        hvx_params.handle = p_hag->move_value_handles.value_handle;
        hvx_params.type   = BLE_GATT_HVX_NOTIFICATION;
        hvx_params.offset = gatts_value.offset;
        hvx_params.p_len  = &gatts_value.len;
        hvx_params.p_data = gatts_value.p_value;

        err_code = sd_ble_gatts_hvx(p_hag->conn_handle, &hvx_params);
        NRF_LOG_INFO("sd_ble_gatts_hvx result: %x. \r\n", err_code); 
    }
    else
    {
        err_code = NRF_ERROR_INVALID_STATE;
        NRF_LOG_INFO("sd_ble_gatts_hvx result: NRF_ERROR_INVALID_STATE. \r\n"); 
    }


    return err_code;
}