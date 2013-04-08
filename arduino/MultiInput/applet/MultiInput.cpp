
#include <stdint.h>

/*
 * Input data (at 9600 bps):
 *
 * Byte 0:
 *   bit 7:  high = external reference, low = default 5v reference
 *   bit 6:  high = use super-fast 8-bit mode (only one input allowed, delay ignored)
 *           low  = use normal multiplexed + delayed mode
 *   bits 0-5: high = use corresponding input number, low = don't use
 * Byte 1-3:
 *   delay in microseconds, most significant byte first (required also for fast mode)
 * Byte 4-5:
 *   baud rate to use / 100 (MSB first)
 */

/*
 * Output data format (at specified baud rate):
 * 
 * First byte:
 * 7 - high bit (1)
 * 6 - missed timing bit (1 for fault)
 * 3-5 - input number 0-5
 * 0-2 - most significant three data bits
 * 
 * Second byte:
 * 7 - low bit (0)
 * 0-6 - least significant seven data bits
 */


// Number of input pins available
#define INPUTS 6

#define INITIAL_BAUD_RATE 9600

// When bit 7 is set the external reference is used
#define EXTERNAL_REFERENCE_BIT 128

// When bit 6 is set the "super-fast 8-bit mode" is used
#define SUPER_FAST_MODE_BIT 64



// Mask for flashing while waiting for commands
#define INITIAL_FLASH_MASK (64+128+256)



// LED signal pin
#define SIGNAL_PIN 13

// Assume clock overflow when less than this value
#define CLOCK_OVERFLOW_LIMIT (1000000000L)


#define TIMING_FAULT_BIT ((unsigned char)(1<<6))
#define INPUT_NUMBER_SHIFT 3


/*
 * If READ_FLASH_PERIOD is defined, then the led state will
 * toggle every READ_FLASH_PERIOD sample reads.
 * Comment out to disable.
 */
#define READ_FLASH_PERIOD 512


#include "WProgram.h"
void setup();
void loop();
void superfast(const uint8_t input);
void writeOutput(unsigned char n, int value, unsigned char timingFault);
unsigned char delayNext();
void error(void);
uint8_t inputs[INPUTS];
uint8_t inputCount;
unsigned long delayMicros;

unsigned long previousTime = 0;
uint16_t sampleCount = 0;
uint8_t ledState = 0;


void setup() {
  unsigned char count = 0;
  unsigned char n;
  unsigned char buf[6];
  unsigned long baudrate;

  // Setup initial serial settings
  Serial.begin(INITIAL_BAUD_RATE);
  pinMode(SIGNAL_PIN, OUTPUT);

  // Read initial data while flashing led  
  while (count < 6) {
    if (Serial.available() > 0) {
      buf[count] = Serial.read();
      count++;
    }

    digitalWrite(SIGNAL_PIN, (millis() & INITIAL_FLASH_MASK) ? LOW : HIGH);
  }
  digitalWrite(SIGNAL_PIN, LOW);

  // Parse inputs byte
  inputCount = 0;
  for (n=0; n < INPUTS; n++) {
    if (buf[0] & (1<<n)) {
      inputs[inputCount] = n;
      inputCount++;
    }
  }

  if (inputCount == 0)
    error();


  // Parse delay
  delayMicros = (buf[1]<<16) | (buf[2]<<8) | (buf[3]);
  if (delayMicros > 5000000)
    error();

  // Parse and set baud rate
  baudrate = (buf[4]<<8) | (buf[5]);
  baudrate = baudrate * 100;
  Serial.begin(baudrate);
  delay(500);
  

  // Check whether to use external or default reference
  analogReference((buf[0] & EXTERNAL_REFERENCE_BIT) ? EXTERNAL : DEFAULT);


  // Check whether to start super-fast mode
  if (buf[0] & SUPER_FAST_MODE_BIT) {
    if (inputCount != 1)
      error();
    digitalWrite(SIGNAL_PIN, HIGH);
    delay(100);
    superfast(inputs[0]);
  }
  

  previousTime = micros();
}



void loop() {
  int values[INPUTS];
  unsigned char i;
  unsigned char timingFault;
  
  // Wait until next moment
  timingFault = delayNext();
  
  // Read inputs as rapidly as possible
  for (i=0; i < inputCount; i++) {
    values[i] = analogRead(inputs[i]);
  }

  // Write output
  for (i=0; i < inputCount; i++) {
    writeOutput(inputs[i], values[i], timingFault);
  }
  
  // Toggle LED
#ifdef READ_FLASH_PERIOD
  sampleCount++;
  if (sampleCount >= READ_FLASH_PERIOD) {
    sampleCount = 0;
    ledState = !ledState;
    digitalWrite(SIGNAL_PIN, ledState ? HIGH : LOW);
  }
#endif

}



/*
 * Super-fast 8-bit mode loop.  Simply read the input and output the highest
 * 8 bits to the serial.
 */
void superfast(const uint8_t input) {
  register unsigned int value;
  
  noInterrupts();
  
  while (true) {
    value = analogRead(input);
    Serial.write((uint8_t)(value >> 2));
  }
}


void writeOutput(unsigned char n, int value, unsigned char timingFault) {
  unsigned char buf[2];
  
  buf[0] = 0x80;
  buf[1] = 0;
  
  if (timingFault)
    buf[0] |= TIMING_FAULT_BIT;
    
  buf[0] |= (n << INPUT_NUMBER_SHIFT);
  
  buf[0] |= (value >> 7);
  buf[1] |= (value & 0x7F);

  Serial.write(buf,2);
}



/*
 * Wait for the next moment to measure the inputs.  Returns true
 * if we are already late, false otherwise.
 */
unsigned char delayNext() {

  unsigned long nextTime;
  unsigned long now;

  // Check for microsecond overflow
  nextTime = previousTime + delayMicros;
  if (nextTime < previousTime) {
    // Overflow occurred, wait for clock to overflow as well
    while (micros() > CLOCK_OVERFLOW_LIMIT)
      ;
  }
  previousTime = nextTime;

  now = micros();

  // Check whether we have already used too much time
  if (now > nextTime) {
    if (now > nextTime + delayMicros) {
      // We can't catch up, accept it
      previousTime = now;
    }
    return true;
  }

  // Wait for the correct time
  while (micros() < nextTime)
    ;
  
  return false;
}



/*
 * Signal an error by irregular flashing (...---...)
 */
void error(void) {
 while (true) {
  digitalWrite(SIGNAL_PIN, HIGH);
  delay(100);
  digitalWrite(SIGNAL_PIN, LOW);
  delay(200);
  digitalWrite(SIGNAL_PIN, HIGH);
  delay(100);
  digitalWrite(SIGNAL_PIN, LOW);
  delay(200);
  digitalWrite(SIGNAL_PIN, HIGH);
  delay(100);
  digitalWrite(SIGNAL_PIN, LOW);
  delay(200);

  digitalWrite(SIGNAL_PIN, HIGH);
  delay(300);
  digitalWrite(SIGNAL_PIN, LOW);
  delay(200);
  digitalWrite(SIGNAL_PIN, HIGH);
  delay(300);
  digitalWrite(SIGNAL_PIN, LOW);
  delay(200);
  digitalWrite(SIGNAL_PIN, HIGH);
  delay(300);
  digitalWrite(SIGNAL_PIN, LOW);
  delay(200);

  digitalWrite(SIGNAL_PIN, HIGH);
  delay(100);
  digitalWrite(SIGNAL_PIN, LOW);
  delay(200);
  digitalWrite(SIGNAL_PIN, HIGH);
  delay(100);
  digitalWrite(SIGNAL_PIN, LOW);
  delay(200);
  digitalWrite(SIGNAL_PIN, HIGH);
  delay(100);
  digitalWrite(SIGNAL_PIN, LOW);
  delay(1000);
 } 
}



int main(void)
{
	init();

	setup();
    
	for (;;)
		loop();
        
	return 0;
}

