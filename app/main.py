from __future__ import annotations

from pathlib import Path
from typing import Dict

import joblib
import numpy as np
import pandas as pd
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field


BASE_DIR = Path(__file__).resolve().parent.parent
MODEL_PATH = BASE_DIR / "model" / "trained_model.joblib"


def _load_model(path: Path):
    if not path.exists():
        raise RuntimeError(
            f"Model file not found at {path}. Ensure training artifacts are available before starting the API."
        )

    try:
        model = joblib.load(path)
    except Exception as exc:  # noqa: BLE001 - surface underlying joblib/scikit errors
        raise RuntimeError(f"Unable to load trained model from {path}") from exc

    feature_names = getattr(model, "feature_names_in_", None)
    if feature_names is None:
        raise RuntimeError(
            "Trained model does not expose 'feature_names_in_'. "
            "Retrain the model with a pandas DataFrame or persist feature metadata alongside the artifact."
        )

    return model, list(feature_names)


MODEL, FEATURE_NAMES = _load_model(MODEL_PATH)


class InjuryFeatures(BaseModel):
    BR1_Head_Neck: float = Field(..., description="Standardised AIS score for head and neck injuries")
    BR2_Face: float = Field(..., description="Standardised AIS score for facial injuries")
    BR3_Thorax: float = Field(..., description="Standardised AIS score for thoracic injuries")
    BR4_Abdomen: float = Field(..., description="Standardised AIS score for abdominal injuries")
    BR5_Extremities: float = Field(..., description="Standardised AIS score for extremity injuries")
    BR6_External: float = Field(..., description="Standardised AIS score for external injuries")
    GCS: float = Field(..., description="Glasgow Coma Scale (scaled)")
    HR_bpm: float = Field(..., description="Heart rate in beats per minute (scaled)")
    ISS: float = Field(..., description="Injury Severity Score (scaled)")
    ISS_category: float = Field(..., description="Categorised ISS (scaled)")
    RR_bpm: float = Field(..., description="Respiratory rate in breaths per minute (scaled)")
    SBP_mmHg: float = Field(..., description="Systolic blood pressure (scaled)")
    SpO2_percent: float = Field(..., description="Oxygen saturation percentage (scaled)")
    altered_consciousness: float = Field(..., description="Indicator for altered consciousness (scaled)")
    has_sepsis: float = Field(..., description="Indicator for presence of sepsis (scaled)")
    hypotension: float = Field(..., description="Indicator for hypotension (scaled)")
    hypoxia: float = Field(..., description="Indicator for hypoxia (scaled)")
    mISS_ISS_diff: float = Field(..., description="Difference between mISS and ISS (scaled)")
    mISS_calculated: float = Field(..., description="Calculated mISS value (scaled)")
    mISS_category: float = Field(..., description="Categorised mISS (scaled)")
    max_AIS: float = Field(..., description="Maximum Abbreviated Injury Scale (scaled)")
    num_abnormal_vitals: float = Field(..., description="Count of abnormal vital signs (scaled)")
    num_regions_injured: float = Field(..., description="Number of injured anatomical regions (scaled)")
    polytrauma: float = Field(..., description="Indicator for polytrauma (scaled)")
    severe_injury: float = Field(..., description="Indicator for severe injury (scaled)")
    severity_discordant: float = Field(..., description="Indicator for discordant severity assessments (scaled)")
    tachycardia: float = Field(..., description="Indicator for tachycardia (scaled)")
    tachypnea: float = Field(..., description="Indicator for tachypnea (scaled)")

    def as_ordered_frame(self) -> pd.DataFrame:
        payload = self.model_dump()
        missing = set(FEATURE_NAMES) - payload.keys()
        if missing:
            raise ValueError(f"Missing required features: {sorted(missing)}")

        ordered = {name: payload[name] for name in FEATURE_NAMES}
        return pd.DataFrame([ordered], columns=FEATURE_NAMES)


OUTCOME_MAPPING: Dict[int, str] = {
    0: "Survived",
    1: "Non-Preventable Death",
    2: "Preventable Death",
}


app = FastAPI(
    title="Injury Outcome Prediction API",
    description="Predict trauma outcomes using the trained RandomForest model.",
    version="0.1.0",
)


@app.get("/health", tags=["health"])
async def health_check() -> Dict[str, str]:
    return {"status": "ok"}


@app.post("/predict", tags=["prediction"])
async def predict_outcome(features: InjuryFeatures) -> Dict[str, object]:
    try:
        data_frame = features.as_ordered_frame().astype(float)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc

    try:
        prediction = MODEL.predict(data_frame)[0]
        probabilities = MODEL.predict_proba(data_frame)[0]
    except Exception as exc:  # noqa: BLE001 - surface any model inference failure
        raise HTTPException(status_code=500, detail="Model inference failed") from exc

    outcome_label = OUTCOME_MAPPING.get(int(prediction), "Unknown")

    probability_payload = {
        OUTCOME_MAPPING.get(class_index, f"Class {class_index}"): float(prob)
        for class_index, prob in enumerate(probabilities)
    }

    return {
        "predicted_class_code": int(prediction),
        "predicted_outcome": outcome_label,
        "predicted_probabilities": probability_payload,
        "feature_order": FEATURE_NAMES,
    }

