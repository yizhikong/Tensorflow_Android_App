import sys
# transform is the model definition file in this case
import transform
import tensorflow as tf
from tensorflow.python.framework import tensor_shape, graph_util
from tensorflow.python.platform import gfile
from argparse import ArgumentParser

parser = ArgumentParser()
parser.add_argument('--ckpt', type=str, help='path of checkpoint',
                    dest='ckpt', metavar='CKPT', required=True)

def _node_name(n):
  if n.startswith("^"):
    return n[1:]
  else:
    return n.split(":")[0]

def ckpt2pb(ckpt_path):
    graph = tf.Graph()
    soft_config = tf.ConfigProto(allow_soft_placement=True)
    soft_config.gpu_options.allow_growth = True
    sess = tf.Session(config=soft_config)
    
    # construct the model first
    batch_shape = (1, 256, 256, 3)
    img_placeholder = tf.placeholder(tf.float32, shape=batch_shape,
                                         name='img_placeholder')
    # transform.net is the func which build and return the model in this case
    # replace it use your model -> output = your_model(img_placeholder)
    output = transform.net(img_placeholder)

    graph_def = sess.graph.as_graph_def()
    for node in graph_def.node:
        print _node_name(node.name)

    # find the ouput tensor name of the model in the print above
    # assumpt it is the last one
    final_tensor_name =_node_name(graph_def.node[-1].name)

    saver = tf.train.Saver()
    saver.restore(sess, ckpt_path)

    output_filename = ckpt_path.replace('ckpt', 'pb')
    output_graph_def = graph_util.convert_variables_to_constants(sess,
                           sess.graph.as_graph_def(), [final_tensor_name])

    with gfile.FastGFile(output_filename, 'wb') as f:
        f.write(output_graph_def.SerializeToString())

if __name__ == '__main__':
    options = parser.parse_args()
    ckpt2pb(options.ckpt)
