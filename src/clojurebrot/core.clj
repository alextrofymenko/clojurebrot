(ns clojurebrot.core
  (:require [quil.core :as q]
            [quil.middleware :as m])
  (:import [java.io File]
           [javax.imageio ImageIO]
           [java.awt Color]
           [java.awt.image BufferedImage]))

(def WIDTH 1280)
(def HEIGHT 800)
(def HALF_WIDTH (/ WIDTH 2))
(def HALF_HEIGHT (/ HEIGHT 2))
(def LIMIT 4)
(def ITERATIONS 50)
(def LOG2 (Math/log 2))

(defn zx-fn [cx x scale] (+ cx (* (- x HALF_WIDTH) scale)))
(defn zy-fn [cy y scale] (+ cy (* (- y HALF_HEIGHT) scale)))
(defn a'-fn [a b zx] (- (* a a) (* b b) (- 0 zx)))
(defn b'-fn [a b zy] (+ (* 2 a b) zy))
(defn z2-fn [a' b'] (+ (* a' a') (* b' b')))
(defn calculate-hue [n z2] (/ (+ n (- 1 (/ (Math/log (Math/log (Math/sqrt z2))) LOG2))) ITERATIONS))

(defn save-image
  [{:keys [cx cy scale]}]
  (let [bi  (BufferedImage. WIDTH HEIGHT BufferedImage/TYPE_INT_ARGB)
        gfx (.createGraphics bi)]
    (.setBackground gfx (Color. 0 0 0))
    (doseq [x (range WIDTH)
            y (range HEIGHT)
            :let [zx (zx-fn cx x scale)
                  zy (zy-fn cy y scale)]]
      (loop [a zx
             b zy
             n 1]
        (let [a' (a'-fn a b zx)
              b' (b'-fn a b zy)
              z2 (z2-fn a' b')]
          (cond
            (> n (dec ITERATIONS)) nil
            (> z2 LIMIT) (let [hue (calculate-hue n z2)]
                           (.setColor gfx (Color. (Color/HSBtoRGB hue 0.8 1)))
                           (.fillRect gfx x y 1 1))
            :else (recur a' b' (inc n))))))
    (ImageIO/write bi "png" (File. "bg.png"))))

(defn setup []
  (q/frame-rate 30)
  (q/color-mode :rgb)
  (let [state {:frames '({:cx    -0.5
                          :cy    0
                          :scale 0.0035
                          :limit 4})
               :mouse  {:x1 nil :y1 nil
                        :x2 nil :y2 nil}}]
    (save-image (first (:frames state)))
    state))

(defn draw-state
  [{:keys [mouse]}]
  (q/background 0)
  (q/background-image (q/load-image "bg.png"))
  (when (not-any? nil? (vals mouse))
    (q/stroke 255)
    (q/no-fill)
    (let [{:keys [x1 y1 x2 y2]} mouse]
      (q/rect x1 y1 (- x2 x1) (- y2 y1)))))

(defn mouse-pressed
  [state {:keys [x y button] :as event}]
  (if (= button :left)
    (-> state
        (assoc-in [:mouse :x1] x)
        (assoc-in [:mouse :y1] y)
        (assoc-in [:mouse :last-button] :left))
    state))

(defn mouse-dragged
  [{:keys [mouse] :as state} {:keys [x button] :as event}]
  (if (and (= button :left)
           (:x1 mouse)
           (> x (:x1 mouse)))
    (-> state
        (assoc-in [:mouse :x2] x)
        (assoc-in [:mouse :y2] (- (:y1 mouse)
                                  (/ (* (- (:x1 mouse) x) HEIGHT)
                                     WIDTH))))
    state))

(defn mouse-released
  [{{x1 :x1 y1 :y1
     x2 :x2 y2 :y2
     lb :last-button} :mouse
    frames          :frames
    :as             state}
   {:keys [button] :as event}]
  (if (= lb :left)
    (let [{:keys [cx cy scale]} (first frames)
         frame {:cx    (+ cx (* scale (- (/ (+ x2 x1) 2) HALF_WIDTH)))
                :cy    (+ cy (* scale (- (/ (+ y2 y1) 2) HALF_HEIGHT)))
                :scale (* scale (/ (- x2 x1) WIDTH))}]
     (save-image frame)
     (-> state
         (update :frames conj frame)
         (assoc :mouse {:x1 nil :y1 nil
                        :x2 nil :y2 nil})))
    state))

(defn mouse-clicked
  [{:keys [frames] :as state} {:keys [button] :as event}]
  (if (and (= button :right)
           (> (count frames) 1))
    (do
      (save-image (nth frames 1))
      (update state :frames #(drop 1 %)))
    state))

(defn -main
  [& args]
  (q/sketch
    :title "Mandelbrot fractal"
    :size [WIDTH HEIGHT]
    :setup setup
    :draw #(time (draw-state %))
    :middleware [m/fun-mode]
    :feature [:exit-on-close]
    :renderer :p2d
    :mouse-pressed mouse-pressed
    :mouse-dragged mouse-dragged
    :mouse-released mouse-released
    :mouse-clicked mouse-clicked))
