(ns lsyscljs.lsystems
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [clojure.data :as data]
            [clojure.string :as string]))

;; L-system info: https://en.wikipedia.org/wiki/L-system

(def app-state (atom {}))

(defn get-description [lsys-type]
  (case lsys-type
    :koch {:start [:clr :F]
           :rules {:F [:F :+ :F :- :F :- :F :+ :F]}
           :angle 90}
    :dragon {:start [:clr :F :X]
             :rules {:X [:X :+ :Y :F]
                     :Y [:F :X :- :Y]}
             :angle 90}
    :sierpinski {:start [:clr :A]
                 :rules {:A [:B :- :A :- :B]
                         :B [:A :+ :B :+ :A]}
                 :angle 60}))

(defn flatten-vec [s]
  (vec (flatten s)))

(defn rotate [ang line-length]
  (let [rad (* ang (/ js/Math.PI 180))
        x (* line-length (js/Math.cos rad))
        y (* -1 line-length (js/Math.sin rad))]
    [x y]))

(defn lsys [start rules iters]
  (loop [state start
         remaining-iterations iters]
    ;; the comprehension substitutes elements according to the grammar and returns a
    ;; sequence of vectors, we just flatten 'em into a vector.
    (let [new-state (flatten-vec (for [elem (seq state)
                                       :let  [supstitution (or (elem rules) [elem])]]
                                   supstitution))]
      (if (> remaining-iterations 0)
        (recur new-state (dec remaining-iterations))
        new-state))))

(defn draw-elem [owner meaning pos ang rotation]
  (let [ctx (.getContext (om/get-node owner "lsysc") "2d")]
    (.moveTo ctx (first pos) (second pos))
    (case meaning
      :fw (let [line-length (:line-length (om/get-state owner :lsys))
                rot (rotate ang line-length)
                new-pos [(+ (first rot) (first pos)) (+ (second rot) (second pos))]]
            (.lineTo ctx (first new-pos) (second new-pos))
            (.stroke ctx)
            {:pos new-pos :ang ang})
      :lt {:pos pos :ang (+ ang rotation)}
      :rt {:pos pos :ang (- ang rotation)}
      :clear {:pos pos :ang ang}
      :noop {:pos pos :ang ang})))

(defn draw [owner system angle]
  (let [lsys (om/get-state owner :lsys)
        current-pos (atom [0 (:canvas-height lsys)])
        current-ang (atom 0)
        ctx (.getContext (om/get-node owner "lsysc") "2d")]
    (.clearRect ctx 0 0 (:canvas-width lsys) (:canvas-height lsys))
    (.beginPath ctx)
    (doseq [elem system
            :let [meaning (elem (om/get-state owner :cmds))
                  update (draw-elem owner meaning @current-pos @current-ang angle)]]
      (reset! current-pos (:pos update))
      (reset! current-ang (:ang update)))))

(defn lsys-view [app owner]
  (reify
    om/IInitState
    (init-state [_]
                {:lsys {:line-length 5
                        :canvas-width 900
                        :canvas-height 800}
                 :cmds {:F :fw :A :fw :B :fw
                        :+ :lt
                        :- :rt
                        :clr :clear
                        :X :noop :Y :noop}
                 :system {:commands [] :angle 0}
                 :command (chan)
                 :index 0})
    om/IWillMount
    (will-mount [_]
               (let [command-chan (om/get-state owner :command)]
                  (go (loop []
                        (let [command (<! command-chan)
                              description (get-description (:type command))
                              commands (lsys (:start description) (:rules description) (:iters command))
                              angle (:angle description)]
                          (om/transact! app :system (fn [_] {:commands commands :angle angle}))
                          (recur))))))
    om/IDidUpdate
    (did-update [_ _ _]
                (let [system (:system app)
                      ctx (.getContext (om/get-node owner "lsysc") "2d")]
                  (draw owner (:commands system) (:angle system))))
    om/IRenderState
    (render-state [this {:keys [lsys cmds command]}]
                  (dom/div nil
                           (dom/canvas #js {:width (:canvas-width lsys)
                                            :height (:canvas-height lsys)
                                            :ref "lsysc"})
                           (dom/button #js {:onClick (fn [e] (put! command {:type :sierpinski :iters 5}))} "sierpinski 5")
                           (dom/button #js {:onClick (fn [e] (put! command {:type :sierpinski :iters 7}))} "sierpinski 7")
                           (dom/button #js {:onClick (fn [e] (put! command {:type :dragon :iters 11}))} "dragon 11")
                           (dom/button #js {:onClick (fn [e] (put! command {:type :koch :iters 4}))} "koch 4")
                           (dom/button #js {:onClick (fn [e] (put! command {:type :koch :iters 3}))} "koch 3")))))

(om/root lsys-view app-state
         {:target (js/document.getElementById "lsys")})
