/**
 * @brief HAG Application main file.
 *
 * This file contains the source code for a server application using the HAG service.
 */

#include <stdint.h>
#include <string.h>
#include "nordic_common.h"
#include "nrf.h"
#include "app_error.h"
#include "ble.h"
#include "ble_err.h"
#include "ble_hci.h"
#include "ble_srv_common.h"
#include "ble_advdata.h"
#include "ble_conn_params.h"
#include "nrf_sdh.h"
#include "nrf_sdh_ble.h"
#include "app_timer.h"
#include "nrf_gpio.h"
#include "nrf_ble_gatt.h"
#include "nrf_ble_qwr.h"
#include "nrf_delay.h"
#include "nrf_log.h"
#include "nrf_log_ctrl.h"
#include "nrf_log_default_backends.h"
#include "HAG_Service.h"
#include "hx711.h"


#define TOO_FAR_SWITCH                  17
#define HOME_SWITCH                     19
#define MOTOR_PIN_1                     13  //13
#define MOTOR_PIN_2                     15  //15
#define MOTOR_PIN_3                     16  //16
#define MOTOR_PIN_4                     20  //20


#define DEVICE_NAME                     "H.A.G. Board"                         /**< Name of device. Will be included in the advertising data. */

#define APP_BLE_OBSERVER_PRIO           3                                       /**< Application's BLE observer priority. You shouldn't need to modify this value. */
#define APP_BLE_CONN_CFG_TAG            1                                       /**< A tag identifying the SoftDevice BLE configuration. */

#define APP_ADV_INTERVAL                64                                      /**< The advertising interval (in units of 0.625 ms; this value corresponds to 40 ms). */
#define APP_ADV_DURATION                BLE_GAP_ADV_TIMEOUT_GENERAL_UNLIMITED   /**< The advertising time-out (in units of seconds). When set to 0, we will never time out. */


#define MIN_CONN_INTERVAL               MSEC_TO_UNITS(100, UNIT_1_25_MS)        /**< Minimum acceptable connection interval (0.5 seconds). */
#define MAX_CONN_INTERVAL               MSEC_TO_UNITS(200, UNIT_1_25_MS)        /**< Maximum acceptable connection interval (1 second). */
#define SLAVE_LATENCY                   0                                       /**< Slave latency. */
#define CONN_SUP_TIMEOUT                MSEC_TO_UNITS(4000, UNIT_10_MS)         /**< Connection supervisory time-out (4 seconds). */

#define NOTIFICATION_INTERVAL           APP_TIMER_TICKS(100)

#define FIRST_CONN_PARAMS_UPDATE_DELAY  APP_TIMER_TICKS(20000)                  /**< Time from initiating event (connect or start of notification) to first time sd_ble_gap_conn_param_update is called (20 seconds). */
#define NEXT_CONN_PARAMS_UPDATE_DELAY   APP_TIMER_TICKS(5000)                   /**< Time between each call to sd_ble_gap_conn_param_update after the first call (5 seconds). */
#define MAX_CONN_PARAMS_UPDATE_COUNT    3                                       /**< Number of attempts before giving up the connection parameter negotiation. */

#define DEAD_BEEF                       0xDEADBEEF                              /**< Value used as error code on stack dump, can be used to identify stack location on stack unwind. */

#define MM_PER_ROT                      10
#define STEPS_PER_ROT                   200
#define MOTOR_DELAY                     10
#define MAX_HOLD_DEPTH                  100
#define HX711_SCALE                     -7050
#define WEIGHT_BUFFER_SIZE              10

NRF_BLE_GATT_DEF(m_gatt);                                                       /**< GATT module instance. */
NRF_BLE_QWR_DEF(m_qwr);

/**< Structure used to identify the HAG service. */
BLE_HAG_SERVICE_DEF(m_hag);          
APP_TIMER_DEF(m_notification_timer_id);                                         /**< Context for the Queued Write module.*/

static uint16_t m_conn_handle = BLE_CONN_HANDLE_INVALID;                        /**< Handle of the current connection. */

static uint8_t m_adv_handle = BLE_GAP_ADV_SET_HANDLE_NOT_SET;                   /**< Advertising handle used to identify an advertising set. */
static uint8_t m_enc_advdata[BLE_GAP_ADV_SET_DATA_SIZE_MAX];                    /**< Buffer for storing an encoded advertising set. */
static uint8_t m_enc_scan_response_data[BLE_GAP_ADV_SET_DATA_SIZE_MAX];         /**< Buffer for storing an encoded scan data. */

static uint8_t m_current_depth = 0;
static uint8_t m_current_angle = 0;
static int m_current_weight = 0;
static int m_weight_buffer[WEIGHT_BUFFER_SIZE];
static uint8_t m_weight_buffer_ind = 0;
static int m_hx711_offset = 42300;  // initial offset
static uint8_t m_weight_array[6];


/**@brief Struct that contains pointers to the encoded advertising data. */
static ble_gap_adv_data_t m_adv_data =
{
    .adv_data =
    {
        .p_data = m_enc_advdata,
        .len    = BLE_GAP_ADV_SET_DATA_SIZE_MAX
    },
    .scan_rsp_data =
    {
        .p_data = m_enc_scan_response_data,
        .len    = BLE_GAP_ADV_SET_DATA_SIZE_MAX

    }
};


/**@brief Function for assert macro callback.
 *
 * @details This function will be called in case of an assert in the SoftDevice.
 *
 * @warning This handler is an example only and does not fit a final product. You need to analyze
 *          how your product is supposed to react in case of Assert.
 * @warning On assert from the SoftDevice, the system can only recover on reset.
 *
 * @param[in] line_num    Line number of the failing ASSERT call.
 * @param[in] p_file_name File name of the failing ASSERT call.
 */
void assert_nrf_callback(uint16_t line_num, const uint8_t * p_file_name)
{
    // TODO: set flag in move char
    app_error_handler(DEAD_BEEF, line_num, p_file_name);
}


/**@brief Function for setting m_hx711_offset
 *
 */
static void hx711_tare(void)
{
    long temp_hx711_offset = 0;

    for (uint8_t i=0; i<WEIGHT_BUFFER_SIZE; i++)
    {
        temp_hx711_offset += m_weight_buffer[i];
    }

    m_hx711_offset = temp_hx711_offset / WEIGHT_BUFFER_SIZE;
    NRF_LOG_INFO("hx711 offset updated to %d", m_hx711_offset);
}


void hx711_callback(hx711_evt_t evt, int value)
{
    if(evt == DATA_READY)
    {
        m_current_weight = (value - m_hx711_offset) / HX711_SCALE;
        m_weight_buffer[m_weight_buffer_ind] = value;
        m_weight_buffer_ind = (m_weight_buffer_ind + 1) % WEIGHT_BUFFER_SIZE;
        NRF_LOG_INFO("ADC measuremement %d", value);
    }
    else
    {
        /*Invalid ADC readout. A non-zero value would indicate that the readout was interrupted 
         by a higher priority interrupt during readout (i.e., Softdevice radio event).
         */
        NRF_LOG_INFO("ADC readout error. %d 0x%x", value, value);
    }
}


/**@brief Function for checking limit switches.
 *
 */
static uint8_t check_limit_switches(void)
{
    if (!nrf_gpio_pin_read(HOME_SWITCH))
    {
        return 1;
    }
    if (!nrf_gpio_pin_read(TOO_FAR_SWITCH))
    {
        return 2;
    }

    return 0;
}


/**@brief Function stepping the motor.
 *
 * The sequence of control signals for 4 control wires is as follows:
 *
 * Step C0 C1 C2 C3
 *    1  1  0  1  0
 *    2  0  1  1  0
 *    3  0  1  0  1
 *    4  1  0  0  1
 *
 */
static void step_motor(int this_step)
{
    switch (this_step)
    {
        case 0:
            nrf_gpio_pin_set(MOTOR_PIN_1);
            nrf_gpio_pin_clear(MOTOR_PIN_2);
            nrf_gpio_pin_set(MOTOR_PIN_3);
            nrf_gpio_pin_clear(MOTOR_PIN_4);
            break;

        case 1:
            nrf_gpio_pin_clear(MOTOR_PIN_1);
            nrf_gpio_pin_set(MOTOR_PIN_2);
            nrf_gpio_pin_set(MOTOR_PIN_3);
            nrf_gpio_pin_clear(MOTOR_PIN_4);
            break;

        case 2:
            nrf_gpio_pin_clear(MOTOR_PIN_1);
            nrf_gpio_pin_set(MOTOR_PIN_2);
            nrf_gpio_pin_clear(MOTOR_PIN_3);
            nrf_gpio_pin_set(MOTOR_PIN_4);
            break;

        case 3:
            nrf_gpio_pin_set(MOTOR_PIN_1);
            nrf_gpio_pin_clear(MOTOR_PIN_2);
            nrf_gpio_pin_clear(MOTOR_PIN_3);
            nrf_gpio_pin_set(MOTOR_PIN_4);
            break;

        default:
            break;
    }
}


/**@brief Function for changing depth of hold.
 *
 */
static void change_depth(int desired_depth)
{
    if (desired_depth > MAX_HOLD_DEPTH)
    {
        return;
    }
    if (m_current_depth != desired_depth)
    {
        int mm_to_move =  desired_depth - m_current_depth;
        int steps_to_move = mm_to_move * STEPS_PER_ROT / MM_PER_ROT;

        while (steps_to_move != 0  && desired_depth <= MAX_HOLD_DEPTH)
        {
            // Check limit switches
            switch (check_limit_switches())
            {
                case 0: // limit switches not triggered
                    if (steps_to_move <= 0)
                    {
                        step_motor(3 - abs(steps_to_move % 4));
                        steps_to_move ++;
                        nrf_delay_ms(MOTOR_DELAY);
                    }
                    else
                    {
                        step_motor(steps_to_move % 4);
                        steps_to_move --;
                        nrf_delay_ms(MOTOR_DELAY);
                    }
                    break;

                case 1: // home limit switch triggered
                    if (steps_to_move < 0)
                    {
                        steps_to_move = 0;
                        m_current_depth = 0;
                    }
                    else
                    {
                        step_motor(steps_to_move % 4);
                        steps_to_move --;
                        nrf_delay_ms(MOTOR_DELAY);
                    }
                    break;

                case 2: // too far (deep) limit switch triggered
                    // TODO: Trigger flag in move char and re-home
                    if (steps_to_move > 0)
                    {
                        steps_to_move = 0;
                    }
                    else
                    {
                        step_motor(3 - abs(steps_to_move % 4));
                        steps_to_move ++;
                        nrf_delay_ms(MOTOR_DELAY);
                    }
                    break;

                default:
                    break;
            }
        }

        m_current_depth = desired_depth;

        // Turn outputs to motor off. This should save the battery but may allow the hold to drift
        nrf_gpio_pin_clear(MOTOR_PIN_1);
        nrf_gpio_pin_clear(MOTOR_PIN_2);
        nrf_gpio_pin_clear(MOTOR_PIN_3);
        nrf_gpio_pin_clear(MOTOR_PIN_4);
    }
    
}


/**@brief Function for handling the weight measurement timer timeout.
 *
 * @details This function will be called each time the weight measurement timer expires.
 *
 * @param[in] p_context  Pointer used for passing some arbitrary information (context) from the
 *                       app_start_timer() call to the timeout handler.
 */
static void notification_timeout_handler(void * p_context)
{
    UNUSED_PARAMETER(p_context);
    ret_code_t err_code;
 
    // Encode m_current_weight into 4 bytes of data
    m_weight_array[0] = m_current_weight & 0x000000ff;
    m_weight_array[1] = (m_current_weight & 0x0000ff00) >> 8;
    m_weight_array[2] = (m_current_weight & 0x00ff0000) >> 16;
    m_weight_array[3] = (m_current_weight & 0xff000000) >> 24;

    uint8_t hagCurrentData [6] = {m_current_angle, m_current_depth, m_weight_array[3], m_weight_array[2], m_weight_array[1], m_weight_array[0]};

    err_code = ble_hag_current_value_update(&m_hag, hagCurrentData);

    if ((err_code != NRF_SUCCESS) &&
        (err_code != NRF_ERROR_INVALID_STATE) &&
        (err_code != NRF_ERROR_RESOURCES) &&
        (err_code != BLE_ERROR_GATTS_SYS_ATTR_MISSING)
       )
    {
        APP_ERROR_HANDLER(err_code);
    }
}


/**@brief Function for the Timer initialization.
 *
 * @details Initializes the timer module.
 */
static void timers_init(void)
{
    // Initialize timer module, making it use the scheduler
    ret_code_t err_code = app_timer_init();
    APP_ERROR_CHECK(err_code);

    // Create timers.
    err_code = app_timer_create(&m_notification_timer_id, APP_TIMER_MODE_REPEATED, notification_timeout_handler);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for the GAP initialization.
 *
 * @details This function sets up all the necessary GAP (Generic Access Profile) parameters of the
 *          device including the device name, appearance, and the preferred connection parameters.
 */
static void gap_params_init(void)
{
    ret_code_t              err_code;
    ble_gap_conn_params_t   gap_conn_params;
    ble_gap_conn_sec_mode_t sec_mode;

    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&sec_mode);

    err_code = sd_ble_gap_device_name_set(&sec_mode,
                                          (const uint8_t *)DEVICE_NAME,
                                          strlen(DEVICE_NAME));
    APP_ERROR_CHECK(err_code);

    memset(&gap_conn_params, 0, sizeof(gap_conn_params));

    gap_conn_params.min_conn_interval = MIN_CONN_INTERVAL;
    gap_conn_params.max_conn_interval = MAX_CONN_INTERVAL;
    gap_conn_params.slave_latency     = SLAVE_LATENCY;
    gap_conn_params.conn_sup_timeout  = CONN_SUP_TIMEOUT;

    err_code = sd_ble_gap_ppcp_set(&gap_conn_params);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for initializing the GATT module.
 */
static void gatt_init(void)
{
    ret_code_t err_code = nrf_ble_gatt_init(&m_gatt, NULL);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for initializing the Advertising functionality.
 *
 * @details Encodes the required advertising data and passes it to the stack.
 *          Also builds a structure to be passed to the stack when starting advertising.
 */
static void advertising_init(void)
{
    ret_code_t    err_code;
    ble_advdata_t advdata;
    ble_advdata_t srdata;

    ble_uuid_t adv_uuids[] = {{BLE_UUID_HAG_SERVICE_BASE_UUID, BLE_UUID_TYPE_VENDOR_BEGIN}};

    // Build and set advertising data.
    memset(&advdata, 0, sizeof(advdata));

    advdata.name_type          = BLE_ADVDATA_FULL_NAME;
    advdata.include_appearance = true;
    advdata.flags              = BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE;


    memset(&srdata, 0, sizeof(srdata));
    srdata.uuids_complete.uuid_cnt = sizeof(adv_uuids) / sizeof(adv_uuids[0]);
    srdata.uuids_complete.p_uuids  = adv_uuids;

    err_code = ble_advdata_encode(&advdata, m_adv_data.adv_data.p_data, &m_adv_data.adv_data.len);
    APP_ERROR_CHECK(err_code);

    err_code = ble_advdata_encode(&srdata, m_adv_data.scan_rsp_data.p_data, &m_adv_data.scan_rsp_data.len);
    APP_ERROR_CHECK(err_code);

    ble_gap_adv_params_t adv_params;

    // Set advertising parameters.
    memset(&adv_params, 0, sizeof(adv_params));

    adv_params.primary_phy     = BLE_GAP_PHY_1MBPS;
    adv_params.duration        = APP_ADV_DURATION;
    adv_params.properties.type = BLE_GAP_ADV_TYPE_CONNECTABLE_SCANNABLE_UNDIRECTED;
    adv_params.p_peer_addr     = NULL;
    adv_params.filter_policy   = BLE_GAP_ADV_FP_ANY;
    adv_params.interval        = APP_ADV_INTERVAL;

    err_code = sd_ble_gap_adv_set_configure(&m_adv_handle, &m_adv_data, &adv_params);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for handling Queued Write Module errors.
 *
 * @details A pointer to this function will be passed to each service which may need to inform the
 *          application about an error.
 *
 * @param[in]   nrf_error   Error code containing information about what went wrong.
 */
static void nrf_qwr_error_handler(uint32_t nrf_error)
{
    APP_ERROR_HANDLER(nrf_error);
}


/**@brief Function for handling write events to the HAG characteristic.
 *
 * @param[in] p_hag_service  Instance of HAG Service to which the write applies.
 * @param[in] hag_state      Written/desired state of the HAG.
 */
static void on_hag_evt(ble_hag_service_t * p_hag_service, ble_hag_evt_t * p_evt)
{
    ret_code_t err_code;
    
    switch(p_evt->evt_type)
    {
        case BLE_HAG_EVT_NOTIFICATION_ENABLED:
             hx711_tare();
             err_code = app_timer_start(m_notification_timer_id, NOTIFICATION_INTERVAL, NULL);
             APP_ERROR_CHECK(err_code);
             break;

        case BLE_HAG_EVT_NOTIFICATION_DISABLED:
            err_code = app_timer_stop(m_notification_timer_id);
            APP_ERROR_CHECK(err_code);
            break;

        case BLE_HAG_EVT_CONNECTED:
            break;

        case BLE_HAG_EVT_DISCONNECTED:
              break;

        case BLE_HAG_EVT_DESIRED_UPDATED:
              change_depth(p_evt->desired_depth);
              break;

        default:
              // No implementation needed.
              break;
    }
}


/**@brief Function for initializing services that will be used by the application.
 */
static void services_init(void)
{
    ret_code_t          err_code;
    nrf_ble_qwr_init_t  qwr_init = {0};
    ble_hag_init_t      hag_init = {0};

    // Initialize Queued Write Module.
    qwr_init.error_handler = nrf_qwr_error_handler;

    err_code = nrf_ble_qwr_init(&m_qwr, &qwr_init);
    APP_ERROR_CHECK(err_code);

    // 1. Initialize the HAG service
    hag_init.evt_handler = on_hag_evt;

    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&hag_init.current_value_char_attr_md.cccd_write_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&hag_init.desired_value_char_attr_md.cccd_write_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&hag_init.move_value_char_attr_md.cccd_write_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&hag_init.current_value_char_attr_md.write_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&hag_init.desired_value_char_attr_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&hag_init.move_value_char_attr_md.read_perm);
 
    err_code = ble_hag_init(&m_hag, &hag_init);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for handling the Connection Parameters Module.
 *
 * @details This function will be called for all events in the Connection Parameters Module that
 *          are passed to the application.
 *
 * @note All this function does is to disconnect. This could have been done by simply
 *       setting the disconnect_on_fail config parameter, but instead we use the event
 *       handler mechanism to demonstrate its use.
 *
 * @param[in] p_evt  Event received from the Connection Parameters Module.
 */
static void on_conn_params_evt(ble_conn_params_evt_t * p_evt)
{
    ret_code_t err_code;

    if (p_evt->evt_type == BLE_CONN_PARAMS_EVT_FAILED)
    {
        err_code = sd_ble_gap_disconnect(m_conn_handle, BLE_HCI_CONN_INTERVAL_UNACCEPTABLE);
        APP_ERROR_CHECK(err_code);
    }
}


/**@brief Function for handling a Connection Parameters error.
 *
 * @param[in] nrf_error  Error code containing information about what went wrong.
 */
static void conn_params_error_handler(uint32_t nrf_error)
{
    APP_ERROR_HANDLER(nrf_error);
}


/**@brief Function for initializing the Connection Parameters module.
 */
static void conn_params_init(void)
{
    ret_code_t             err_code;
    ble_conn_params_init_t cp_init;

    memset(&cp_init, 0, sizeof(cp_init));

    cp_init.p_conn_params                  = NULL;
    cp_init.first_conn_params_update_delay = FIRST_CONN_PARAMS_UPDATE_DELAY;
    cp_init.next_conn_params_update_delay  = NEXT_CONN_PARAMS_UPDATE_DELAY;
    cp_init.max_conn_params_update_count   = MAX_CONN_PARAMS_UPDATE_COUNT;
    cp_init.start_on_notify_cccd_handle    = BLE_GATT_HANDLE_INVALID;
    cp_init.disconnect_on_fail             = false;
    cp_init.evt_handler                    = on_conn_params_evt;
    cp_init.error_handler                  = conn_params_error_handler;

    err_code = ble_conn_params_init(&cp_init);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for starting advertising.
 */
static void advertising_start(void)
{
    ret_code_t           err_code;

    err_code = sd_ble_gap_adv_start(m_adv_handle, APP_BLE_CONN_CFG_TAG);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for handling BLE events.
 *
 * @param[in]   p_ble_evt   Bluetooth stack event.
 * @param[in]   p_context   Unused.
 */
static void ble_evt_handler(ble_evt_t const * p_ble_evt, void * p_context)
{
    ret_code_t err_code;

    switch (p_ble_evt->header.evt_id)
    {
        case BLE_GAP_EVT_CONNECTED:
            NRF_LOG_INFO("Connected");
            m_conn_handle = p_ble_evt->evt.gap_evt.conn_handle;
            err_code = nrf_ble_qwr_conn_handle_assign(&m_qwr, m_conn_handle);
            APP_ERROR_CHECK(err_code);
            break;

        case BLE_GAP_EVT_DISCONNECTED:
            NRF_LOG_INFO("Disconnected");
            m_conn_handle = BLE_CONN_HANDLE_INVALID;
            advertising_start();
            break;

        case BLE_GAP_EVT_SEC_PARAMS_REQUEST:
            // Pairing not supported
            err_code = sd_ble_gap_sec_params_reply(m_conn_handle,
                                                   BLE_GAP_SEC_STATUS_PAIRING_NOT_SUPP,
                                                   NULL,
                                                   NULL);
            APP_ERROR_CHECK(err_code);
            break;

        case BLE_GAP_EVT_PHY_UPDATE_REQUEST:
        {
            NRF_LOG_DEBUG("PHY update request.");
            ble_gap_phys_t const phys =
            {
                .rx_phys = BLE_GAP_PHY_AUTO,
                .tx_phys = BLE_GAP_PHY_AUTO,
            };
            err_code = sd_ble_gap_phy_update(p_ble_evt->evt.gap_evt.conn_handle, &phys);
            APP_ERROR_CHECK(err_code);
        } break;

        case BLE_GATTS_EVT_SYS_ATTR_MISSING:
            // No system attributes have been stored.
            err_code = sd_ble_gatts_sys_attr_set(m_conn_handle, NULL, 0, 0);
            APP_ERROR_CHECK(err_code);
            break;

        case BLE_GATTC_EVT_TIMEOUT:
            // Disconnect on GATT Client timeout event.
            NRF_LOG_DEBUG("GATT Client Timeout.");
            err_code = sd_ble_gap_disconnect(p_ble_evt->evt.gattc_evt.conn_handle,
                                             BLE_HCI_REMOTE_USER_TERMINATED_CONNECTION);
            APP_ERROR_CHECK(err_code);
            break;

        case BLE_GATTS_EVT_TIMEOUT:
            // Disconnect on GATT Server timeout event.
            NRF_LOG_DEBUG("GATT Server Timeout.");
            err_code = sd_ble_gap_disconnect(p_ble_evt->evt.gatts_evt.conn_handle,
                                             BLE_HCI_REMOTE_USER_TERMINATED_CONNECTION);
            APP_ERROR_CHECK(err_code);
            break;

        default:
            // No implementation needed.
            break;
    }
}


/**@brief Function for initializing the BLE stack.
 *
 * @details Initializes the SoftDevice and the BLE event interrupt.
 */
static void ble_stack_init(void)
{
    ret_code_t err_code;

    err_code = nrf_sdh_enable_request();
    APP_ERROR_CHECK(err_code);

    // Configure the BLE stack using the default settings.
    // Fetch the start address of the application RAM.
    uint32_t ram_start = 0;
    err_code = nrf_sdh_ble_default_cfg_set(APP_BLE_CONN_CFG_TAG, &ram_start);
    APP_ERROR_CHECK(err_code);

    // Enable BLE stack.
    err_code = nrf_sdh_ble_enable(&ram_start);
    APP_ERROR_CHECK(err_code);

    // Register a handler for BLE events.
    NRF_SDH_BLE_OBSERVER(m_ble_observer, APP_BLE_OBSERVER_PRIO, ble_evt_handler, NULL);
}


/**@brief Function for configuring GPIO.
 */
static void gpio_config(void)
{
    // motor driver
    nrf_gpio_cfg_output(MOTOR_PIN_1);
    nrf_gpio_cfg_output(MOTOR_PIN_2);
    nrf_gpio_cfg_output(MOTOR_PIN_3);
    nrf_gpio_cfg_output(MOTOR_PIN_4);

    // limit switches
    nrf_gpio_cfg_input(HOME_SWITCH, NRF_GPIO_PIN_PULLUP);
    nrf_gpio_cfg_input(TOO_FAR_SWITCH, NRF_GPIO_PIN_PULLUP);
}


static void log_init(void)
{
    ret_code_t err_code = NRF_LOG_INIT(NULL);
    APP_ERROR_CHECK(err_code);

    NRF_LOG_DEFAULT_BACKENDS_INIT();
}


/**@brief Function for initializing power management.
 */
static void power_management_init(void)
{
    ret_code_t err_code;
    err_code = nrf_pwr_mgmt_init();
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for handling the idle state (main loop).
 *
 * @details If there is no pending log operation, then sleep until next the next event occurs.
 */
static void idle_state_handle(void)
{
    if (NRF_LOG_PROCESS() == false)
    {
        nrf_pwr_mgmt_run();
    }
}

/**@brief Function for "homing" the stepper motor
 *
 */
static void home_stepper_motor(void)
{
    change_depth(-500);
    NRF_LOG_INFO("Homing stepper motor");
}


/**@brief Function initializing the values in m_weight_buffer
 *
 * @details This is to prevent the buffer from not being full prior to hx711_tare() being called
 */
static void weight_buffer_init(void)
{
    for (uint8_t i=0; i<WEIGHT_BUFFER_SIZE; i++)
    {
        m_weight_buffer[i] = m_hx711_offset;
    }
}


/**@brief Function for application main entry.
 */
int main(void)
{
    // Initialize.
    log_init();
    timers_init();
    power_management_init();
    ble_stack_init();
    gap_params_init();
    gatt_init();
    services_init();
    advertising_init();
    conn_params_init();
    gpio_config();
    hx711_init(INPUT_CH_A_128, hx711_callback);
    weight_buffer_init();

    /* Start continous sampling. Sampling rate is either
       10Hz or 80 Hz, depending on hx711 HW configuration*/
    hx711_start(false); // HW set to 10Hz
//    home_stepper_motor();

    // Start execution.
    NRF_LOG_INFO("HAG started.");

    advertising_start();

    // Enter main loop.
    for (;;)
    {
        idle_state_handle();
    }
}


/**
 * @}
 */
