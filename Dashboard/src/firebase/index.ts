import { initializeApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';
import { firebaseConfig } from './firebaseConfig.js';

let app;
let db: ReturnType<typeof getFirestore> | null = null;

try {
  // Only initialize if the user has replaced the placeholder
  if (firebaseConfig.apiKey && firebaseConfig.apiKey !== "YOUR_API_KEY") {
    app = initializeApp(firebaseConfig);
    db = getFirestore(app);
  } else {
    console.warn("Firebase is not configured. Please update src/firebase/firebaseConfig.js");
  }
} catch (error) {
  console.error("Firebase initialization error", error);
}

export { db };
