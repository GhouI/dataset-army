# Hackathon Trauma Outcome API

This repository packages the trained RandomForest trauma outcome classifier behind a FastAPI service. The service exposes a `/predict` endpoint that mirrors the preprocessing performed in `script.py`, then returns the predicted outcome class together with class probabilities.

## Project Structure

- `app/` – FastAPI application code (see `app/main.py`).
- `model/` – Persisted model artifacts (`trained_model.joblib`).
- `requirements.txt` – Python dependencies pinned to the training environment.
- `Dockerfile` & `docker-compose.yml` – Containerisation assets for local or remote deployment.

## Running Locally

### 1. Create a virtual environment (optional but recommended)

```bash
python3 -m venv .venv
source .venv/bin/activate
```

### 2. Install dependencies

```bash
pip install --upgrade pip
pip install -r requirements.txt
```

### 3. Start the API

```bash
uvicorn app.main:app --reload
```

The API will be available at `http://127.0.0.1:8000`. Navigate to `/docs` for interactive Swagger documentation.

## Running with Docker

### Build

```bash
docker build -t hackathon-model-api .
```

### Run

```bash
docker run --rm -p 8000:8000 hackathon-model-api
```

Or, using Docker Compose:

```bash
docker compose up --build
```

Compose maps the container's port 8000 to host port 8001, so the API is then accessible via `http://127.0.0.1:8001`.

## Sample Request

Send a POST request to `/predict` with all required features:

```bash
curl -X POST http://127.0.0.1:8000/predict \
  -H "Content-Type: application/json" \
  -d '{
        "BR1_Head_Neck": -0.2,
        "BR2_Face": 0.1,
        "BR3_Thorax": 0.0,
        "BR4_Abdomen": 0.5,
        "BR5_Extremities": -0.6,
        "BR6_External": -0.1,
        "GCS": -0.4,
        "HR_bpm": 0.6,
        "ISS": -0.8,
        "ISS_category": -1.2,
        "RR_bpm": 0.1,
        "SBP_mmHg": -0.4,
        "SpO2_percent": 0.7,
        "altered_consciousness": 0.0,
        "has_sepsis": 0.0,
        "hypotension": 0.0,
        "hypoxia": 0.0,
        "mISS_ISS_diff": 0.12,
        "mISS_calculated": -0.69,
        "mISS_category": -1.27,
        "max_AIS": -0.51,
        "num_abnormal_vitals": 0.79,
        "num_regions_injured": -0.08,
        "polytrauma": -0.05,
        "severe_injury": -1.39,
        "severity_discordant": -0.25,
        "tachycardia": 0.50,
        "tachypnea": 0.61
     }'
```

The response includes the outcome class code, descriptive label, and per-class probabilities.

## Notes

- The model artifact must remain at `model/trained_model.joblib` unless `MODEL_PATH` is updated in `app/main.py`.
- `scikit-learn==1.6.1` is pinned to match the environment used to train the model and avoid compatibility warnings.

