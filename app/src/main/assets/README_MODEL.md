# Gemma Model Setup Instructions

## Model Required
This application requires a Gemma model in .task format compatible with MediaPipe LLM Inference API.

## Model Required
This application uses the Gemma 2 2B Instruction-Tuned model in .bin format.

## Download Instructions

You have downloaded: `gemma-2b-it-cpu-int4.bin`
1. Place this file in `app/src/main/assets/` with the exact filename

## Model Specifications
- **Model**: Gemma 2 2B Instruction-Tuned
- **Size**: ~1.3GB (INT4 quantized)
- **Format**: Binary model file (.bin)
- **Framework**: MediaPipe LLM Inference API

## Important Notes
- The model file is NOT included in the repository due to its size
- You must place the downloaded `gemma-2b-it-cpu-int4.bin` file in the assets directory
- .bin format is optimized for MediaPipe's on-device inference
- Ensure your device has at least 2GB of free RAM

## Verification
After placing the model file, verify:
1. File name: `gemma-2b-it-cpu-int4.bin`
2. Location: `app/src/main/assets/`
3. Size: Should be around 1.3GB

## Troubleshooting
If the model fails to load:
1. Check the file name matches exactly (`gemma-2b-it-cpu-int4.bin`)
2. Ensure the file is in the correct directory (`app/src/main/assets/`)
3. Verify you have enough device memory (at least 2GB free RAM)
4. Check logcat for specific error messages
5. Ensure MediaPipe tasks-genai library is properly integrated
6. Sync Gradle if imports are unresolved