# Body Parts Vitals Android App

A Kotlin Android application for selecting body parts, entering vital signs, and submitting data to an AI model for analysis.

## Features

- **Interactive Body Part Selection**: Visual representation of human body with clickable regions
- **Red Highlighting**: Selected body parts are highlighted in red for clear visual feedback
- **Vitals Entry**: Comprehensive form for entering vital signs including:
  - Temperature (°F)
  - Heart Rate (bpm)
  - Blood Pressure
  - Pain Level (1-10)
  - Additional Notes
- **AI Integration**: Submit collected data to an AI model API for analysis
- **Material Design**: Modern UI with Material Design components

## Screenshots

### Main Screen - Body Part Selection
Users can tap on different body parts (Head, Chest, Abdomen, Left/Right Arms, Left/Right Legs) to select them. Selected parts turn red.

### Vitals Entry Screen
For each selected body part, users can enter relevant vital signs and health information.

### AI Submission
Data is submitted to an AI model API and results are displayed to the user.

## Architecture

- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Architecture Pattern**: Repository Pattern with Coroutines
- **Networking**: Retrofit + OkHttp
- **UI**: View Binding + Material Components

## Project Structure

```
app/
├── src/main/
│   ├── java/com/bodyparts/vitals/
│   │   ├── model/
│   │   │   └── BodyPart.kt          # Data models
│   │   ├── network/
│   │   │   ├── AIService.kt         # Retrofit API interface
│   │   │   └── RetrofitClient.kt    # Network client setup
│   │   ├── repository/
│   │   │   └── VitalsRepository.kt  # Data repository
│   │   ├── view/
│   │   │   └── BodyPartsView.kt     # Custom view for body parts
│   │   ├── MainActivity.kt          # Body part selection screen
│   │   └── VitalsEntryActivity.kt   # Vitals entry screen
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml
│   │   │   ├── activity_vitals_entry.xml
│   │   │   └── vitals_form_item.xml
│   │   └── values/
│   │       ├── strings.xml
│   │       ├── colors.xml
│   │       └── themes.xml
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## Setup Instructions

### 1. Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 8 or higher
- Android SDK with API level 34

### 2. Clone the Repository
```bash
git clone <repository-url>
cd dataset-army
```

### 3. Configure AI API Endpoint

**IMPORTANT**: Before running the app, you need to configure your AI model API endpoint.

Edit `app/src/main/java/com/bodyparts/vitals/network/RetrofitClient.kt`:

```kotlin
private const val BASE_URL = "https://your-ai-api-endpoint.com/"
```

Replace with your actual AI API endpoint URL.

### 4. API Contract

The app expects the AI API to accept POST requests to `/api/analyze` with the following JSON structure:

**Request:**
```json
{
  "timestamp": 1234567890,
  "selectedBodyParts": [
    {
      "id": "head",
      "name": "Head",
      "isSelected": true,
      "vitals": {
        "bodyPartId": "head",
        "temperature": "98.6",
        "heartRate": "72",
        "bloodPressure": "120/80",
        "painLevel": "3",
        "notes": "Mild headache"
      }
    }
  ],
  "vitalsData": [
    {
      "bodyPartId": "head",
      "temperature": "98.6",
      "heartRate": "72",
      "bloodPressure": "120/80",
      "painLevel": "3",
      "notes": "Mild headache"
    }
  ]
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Analysis completed successfully",
  "diagnosis": "Based on the vitals provided...",
  "recommendations": [
    "Get adequate rest",
    "Stay hydrated",
    "Monitor symptoms"
  ]
}
```

### 5. Build and Run

1. Open the project in Android Studio
2. Let Gradle sync complete
3. Connect an Android device or start an emulator
4. Click Run (Shift+F10) or use the Run button

## Usage

1. **Select Body Parts**
   - Launch the app
   - Tap on body parts you want to select
   - Selected parts will turn red
   - Tap again to deselect
   - Use "Clear" button to deselect all

2. **Enter Vitals**
   - Tap "Next: Enter Vitals" button
   - Fill in vital signs for each selected body part
   - At least one field must be filled per body part
   - Add any additional notes

3. **Submit to AI**
   - Tap "Submit to AI" button
   - Wait for analysis (progress dialog will show)
   - View results in dialog
   - Results include diagnosis and recommendations

## Dependencies

- **AndroidX Core**: Core Android functionality
- **Material Components**: Modern UI components
- **Lifecycle Components**: ViewModel and LiveData
- **Coroutines**: Asynchronous programming
- **Retrofit**: HTTP client for API calls
- **OkHttp**: Network layer
- **Gson**: JSON parsing

## Customization

### Adding More Body Parts

Edit `app/src/main/java/com/bodyparts/vitals/view/BodyPartsView.kt`:

```kotlin
private fun initializeBodyParts() {
    bodyParts.addAll(
        listOf(
            BodyPart("new_part", "New Part", RectF()),
            // Add more parts here
        )
    )
}

// Define region in onSizeChanged()
bodyParts.find { it.id == "new_part" }?.region?.set(...)
```

### Changing Highlight Color

Edit `app/src/main/java/com/bodyparts/vitals/view/BodyPartsView.kt`:

```kotlin
private val selectedColor = Color.RED  // Change to any color
```

Or use color resources in `app/src/main/res/values/colors.xml`.

### Customizing Vitals Fields

Edit `app/src/main/res/layout/vitals_form_item.xml` to add or remove input fields.

Update `VitalsEntryActivity.kt` to handle the new fields in `validateAndCollectData()`.

## Error Handling

- Network errors are caught and displayed to the user
- Form validation ensures at least one vital is entered per body part
- API errors show detailed error messages
- Loading states prevent double submissions

## Testing

### Manual Testing Checklist
- [ ] Body parts selection/deselection works
- [ ] Selected parts turn red
- [ ] Clear button works
- [ ] Navigation to vitals screen works
- [ ] Form validation works
- [ ] AI submission works (with valid API)
- [ ] Error handling displays properly
- [ ] Back navigation works

## Known Issues

- API endpoint needs to be configured before use
- No offline storage (data is only sent to API)
- Portrait orientation only

## Future Enhancements

- [ ] Add more detailed body part mapping
- [ ] Save data locally (Room database)
- [ ] Add history of previous submissions
- [ ] Support landscape orientation
- [ ] Add image capture for affected areas
- [ ] Multi-language support
- [ ] Dark theme support
- [ ] Offline mode with sync

## License

[Add your license here]

## Contributing

[Add contributing guidelines here]

## Contact

[Add contact information here]

## Troubleshooting

### Build Errors
- Ensure Android SDK is properly installed
- Check that all dependencies are downloaded
- Try "File > Invalidate Caches and Restart"

### Network Errors
- Verify API endpoint is correct
- Check internet permissions in AndroidManifest.xml
- Ensure device has internet connectivity
- Check API server is running and accessible

### UI Issues
- Clear app data and reinstall
- Check device Android version (min API 24)
- Verify layout XML files are properly formatted
