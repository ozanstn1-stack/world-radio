import React, { useRef, useState, useMemo } from 'react';
import {
  View,
  StyleSheet,
  Text,
  TouchableOpacity,
  Dimensions,
  PanResponder,
  GestureResponderEvent,
  PanResponderGestureState
} from 'react-native';
import { Canvas, useFrame } from '@react-three/fiber/native';
import * as THREE from 'three';

export interface RadioStation {
  stationuuid: string;
  name: string;
  country: string;
  state?: string;
  tags?: string;
  latitude: number;
  longitude: number;
  favicon?: string;
}

interface GlobeScreenProps {
  stations: RadioStation[];
  onSelectStation?: (station: RadioStation) => void;
}

/**
 * Converts geographical (lat, lon) degrees to 3D Cartesian coordinates (x, y, z) on a sphere
 */
function latLonToVector3(lat: number, lon: number, radius: number): THREE.Vector3 {
  const phi = (90 - lat) * (Math.PI / 180);
  const theta = (lon + 180) * (Math.PI / 180);

  const x = -(radius * Math.sin(phi) * Math.cos(theta));
  const z = radius * Math.sin(phi) * Math.sin(theta);
  const y = radius * Math.cos(phi);

  return new THREE.Vector3(x, y, z);
}

/**
 * 3D Globe Mesh & Station Markers rendered inside react-three-fiber Canvas
 */
function GlobeScene({
  stations,
  rotation,
  zoom,
  onMarkerPress
}: {
  stations: RadioStation[];
  rotation: { x: number; y: number };
  zoom: number;
  onMarkerPress: (station: RadioStation) => void;
}) {
  const globeGroupRef = useRef<THREE.Group>(null);
  const globeRadius = 2.2;

  // Smoothly apply rotation angles from touch gestures
  useFrame(() => {
    if (globeGroupRef.current) {
      globeGroupRef.current.rotation.x = rotation.x;
      globeGroupRef.current.rotation.y = rotation.y;
      globeGroupRef.current.scale.set(zoom, zoom, zoom);
    }
  });

  // Calculate 3D points for all stations
  const markerPositions = useMemo(() => {
    return stations.map((st) => ({
      station: st,
      position: latLonToVector3(st.latitude, st.longitude, globeRadius + 0.03)
    }));
  }, [stations]);

  return (
    <group ref={globeGroupRef}>
      {/* 1. Base Earth Sphere */}
      <mesh>
        <sphereGeometry args={[globeRadius, 64, 64]} />
        <meshStandardMaterial
          color="#0d1b2a"
          roughness={0.7}
          metalness={0.1}
        />
      </mesh>

      {/* 2. Atmosphere Glow Rim */}
      <mesh>
        <sphereGeometry args={[globeRadius * 1.04, 32, 32]} />
        <meshBasicMaterial
          color="#00e5ff"
          transparent
          opacity={0.12}
          side={THREE.BackSide}
        />
      </mesh>

      {/* 3. Wireframe / Longitude-Latitude Grid */}
      <mesh>
        <sphereGeometry args={[globeRadius * 1.002, 24, 24]} />
        <meshBasicMaterial
          color="#1e3a8a"
          wireframe
          transparent
          opacity={0.25}
        />
      </mesh>

      {/* 4. Station Markers */}
      {markerPositions.map(({ station, position }) => (
        <group key={station.stationuuid} position={position}>
          <mesh
            onClick={() => onMarkerPress(station)}
          >
            <sphereGeometry args={[0.045, 16, 16]} />
            <meshBasicMaterial color="#38bdf8" />
          </mesh>
          {/* Subtle halo ring */}
          <mesh>
            <ringGeometry args={[0.05, 0.08, 16]} />
            <meshBasicMaterial
              color="#00e5ff"
              transparent
              opacity={0.6}
              side={THREE.DoubleSide}
            />
          </mesh>
        </group>
      ))}
    </group>
  );
}

/**
 * Main GlobeScreen Component with Touch-based Rotation and Pinch/Drag Gestures
 */
export default function GlobeScreen({ stations = [], onSelectStation }: GlobeScreenProps) {
  const [rotation, setRotation] = useState({ x: 0.3, y: 0.8 });
  const [zoom, setZoom] = useState(1.0);
  const [selectedStation, setSelectedStation] = useState<RadioStation | null>(null);

  const prevRotation = useRef({ x: 0.3, y: 0.8 });
  const initialDistance = useRef<number | null>(null);
  const initialZoom = useRef(1.0);

  // PanResponder to handle multi-touch rotate and pinch-to-zoom
  const panResponder = useMemo(
    () =>
      PanResponder.create({
        onStartShouldSetPanResponder: () => true,
        onMoveShouldSetPanResponder: () => true,

        onPanResponderGrant: () => {
          prevRotation.current = { ...rotation };
          initialDistance.current = null;
        },

        onPanResponderMove: (evt: GestureResponderEvent, gestureState: PanResponderGestureState) => {
          const touches = evt.nativeEvent.touches;

          if (touches.length === 1) {
            // Single finger drag -> Rotate Globe
            const sensitivity = 0.005;
            const newRotY = prevRotation.current.y + gestureState.dx * sensitivity;
            const newRotX = Math.max(-1.4, Math.min(1.4, prevRotation.current.x + gestureState.dy * sensitivity));

            setRotation({ x: newRotX, y: newRotY });
          } else if (touches.length === 2) {
            // Two finger pinch -> Zoom In / Out
            const touch1 = touches[0];
            const touch2 = touches[1];
            const dx = touch1.pageX - touch2.pageX;
            const dy = touch1.pageY - touch2.pageY;
            const distance = Math.sqrt(dx * dx + dy * dy);

            if (initialDistance.current === null) {
              initialDistance.current = distance;
              initialZoom.current = zoom;
            } else {
              const factor = distance / initialDistance.current;
              const newZoom = Math.max(0.6, Math.min(2.5, initialZoom.current * factor));
              setZoom(newZoom);
            }
          }
        },

        onPanResponderRelease: () => {
          initialDistance.current = null;
        }
      }),
    [rotation, zoom]
  );

  const handleMarkerPress = (station: RadioStation) => {
    setSelectedStation(station);
    onSelectStation?.(station);
  };

  return (
    <View style={styles.container}>
      {/* 3D Canvas Layer with Pan Gestures */}
      <View style={styles.canvasContainer} {...panResponder.panHandlers}>
        <Canvas camera={{ position: [0, 0, 6], fov: 45 }}>
          <ambientLight intensity={0.8} />
          <directionalLight position={[10, 10, 5]} intensity={1.2} />
          <pointLight position={[-10, -10, -5]} intensity={0.5} />
          <GlobeScene
            stations={stations}
            rotation={rotation}
            zoom={zoom}
            onMarkerPress={handleMarkerPress}
          />
        </Canvas>
      </View>

      {/* Top Header Overlay */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>World Radio 3D</Text>
        <Text style={styles.headerSubtitle}>
          {stations.length} stations • Drag to rotate, pinch to zoom
        </Text>
      </View>

      {/* Zoom Control Buttons */}
      <View style={styles.controls}>
        <TouchableOpacity
          style={styles.controlButton}
          onPress={() => setZoom((z) => Math.min(2.5, z + 0.2))}
        >
          <Text style={styles.controlText}>+</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.controlButton}
          onPress={() => setZoom((z) => Math.max(0.6, z - 0.2))}
        >
          <Text style={styles.controlText}>−</Text>
        </TouchableOpacity>
      </View>

      {/* Selected Station Bottom Card */}
      {selectedStation && (
        <View style={styles.stationCard}>
          <View style={styles.stationInfo}>
            <Text style={styles.stationName} numberOfLines={1}>
              {selectedStation.name}
            </Text>
            <Text style={styles.stationLocation}>
              {selectedStation.state ? `${selectedStation.state}, ` : ''}
              {selectedStation.country}
            </Text>
            {selectedStation.tags && (
              <Text style={styles.stationTags} numberOfLines={1}>
                {selectedStation.tags.split(',').slice(0, 3).join(' • ')}
              </Text>
            )}
          </View>
          <TouchableOpacity
            style={styles.playButton}
            onPress={() => onSelectStation?.(selectedStation)}
          >
            <Text style={styles.playButtonText}>Play</Text>
          </TouchableOpacity>
        </View>
      )}
    </View>
  );
}

const { width } = Dimensions.get('window');

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#040814'
  },
  canvasContainer: {
    flex: 1
  },
  header: {
    position: 'absolute',
    top: 50,
    left: 20,
    right: 20,
    backgroundColor: 'rgba(15, 23, 42, 0.75)',
    borderRadius: 16,
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderWidth: 1,
    borderColor: 'rgba(56, 189, 248, 0.2)'
  },
  headerTitle: {
    color: '#ffffff',
    fontSize: 18,
    fontWeight: 'bold'
  },
  headerSubtitle: {
    color: '#94a3b8',
    fontSize: 12,
    marginTop: 2
  },
  controls: {
    position: 'absolute',
    right: 20,
    top: '40%',
    gap: 12
  },
  controlButton: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: 'rgba(15, 23, 42, 0.85)',
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: 'rgba(56, 189, 248, 0.4)'
  },
  controlText: {
    color: '#38bdf8',
    fontSize: 22,
    fontWeight: 'bold',
    lineHeight: 24
  },
  stationCard: {
    position: 'absolute',
    bottom: 30,
    left: 20,
    right: 20,
    backgroundColor: 'rgba(15, 23, 42, 0.92)',
    borderRadius: 20,
    padding: 16,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderWidth: 1,
    borderColor: 'rgba(56, 189, 248, 0.3)'
  },
  stationInfo: {
    flex: 1,
    marginRight: 12
  },
  stationName: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: 'bold'
  },
  stationLocation: {
    color: '#38bdf8',
    fontSize: 13,
    marginTop: 2
  },
  stationTags: {
    color: '#64748b',
    fontSize: 11,
    marginTop: 4
  },
  playButton: {
    backgroundColor: '#0284c7',
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderRadius: 14
  },
  playButtonText: {
    color: '#ffffff',
    fontSize: 14,
    fontWeight: 'bold'
  }
});
